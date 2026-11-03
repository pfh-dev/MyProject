<!--
  AI 智能对话页：类似微信/客服的聊天界面，左侧是会话列表，右侧是消息区。
  核心难点是「流式输出」——通过 fetch 读取后端返回的 SSE 流，把 AI 回复逐字渲染出来。
-->
<template>
  <div class="ai-chat">
    <!-- 左侧栏：品牌标识 + 新建会话按钮 + 历史会话列表 -->
    <aside class="sidebar">
      <div class="brand">
        <span>🐾</span>
        <span>AI 宠物助手「小安」</span>
      </div>
      <el-button class="new-btn" type="primary" plain @click="newSession">＋ 新建会话</el-button>
      <div class="session-list">
        <!-- 会话项：点击切换，右侧小图标删除（.stop 阻止冒泡，避免误触切换） -->
        <div
          v-for="s in sessions"
          :key="s.id"
          class="session-item"
          :class="{ active: s.id === currentId }"
          @click="switchSession(s.id)"
        >
          <span class="session-title text-ellipsis">{{ s.title }}</span>
          <el-icon class="del" @click.stop="deleteSession(s.id)"><Close /></el-icon>
        </div>
      </div>
    </aside>

    <!-- 右侧聊天主区域 -->
    <main class="chat">
      <header class="chat-header">
        <h1>{{ activeSession?.title || '新会话' }}</h1>
        <!-- 在线状态提示，加载中显示「思考中…」 -->
        <span class="status" :class="{ busy: loading }">{{ loading ? '思考中…' : '在线' }}</span>
      </header>

      <!-- 消息列表：空态显示引导语，否则逐条渲染气泡 -->
      <div ref="listRef" class="chat-list">
        <div v-if="!messages.length" class="empty">
          <div class="empty-icon">🐶</div>
          <p>我是小安，可以帮你解答宠物领养、喂养、救助等问题</p>
          <p>试试问我："有哪些适合新手的狗狗？"</p>
        </div>

        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
          <div class="bubble">{{ m.content }}</div>
        </div>
      </div>

      <!-- 输入区：Enter 发送、Shift+Enter 换行 -->
      <footer class="chat-input">
        <el-input
          v-model="input"
          type="textarea"
          :rows="1"
          resize="none"
          placeholder="输入消息，Enter 发送，Shift+Enter 换行"
          @keydown="onKeydown"
        />
        <el-button type="primary" :disabled="loading || !input.trim()" @click="sendMessage">发送</el-button>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import { useUserStore } from '@/stores/user'
import { getSessions, saveSession, getMessages } from '@/api/ai'

const router = useRouter()
const tokenStore = useTokenStore()
const userStore = useUserStore()

// 响应式状态
const sessions = ref([])   // 会话列表
const currentId = ref(null) // 当前会话 id
const messages = ref([])    // 当前会话的消息数组 [{ role: 'user'|'assistant', content }]
const input = ref('')       // 输入框内容
const loading = ref(false)  // 是否正在等待 AI 回复
const listRef = ref(null)   // 消息列表 DOM 引用，用于自动滚动到底部

// 计算属性：根据 currentId 找到当前激活的会话对象
const activeSession = computed(() => sessions.value.find((s) => s.id === currentId.value))

// 生成唯一会话 id：优先用浏览器原生 UUID，否则用时间戳+随机数兜底
function genId() {
  if (window.crypto && crypto.randomUUID) return crypto.randomUUID()
  return 's' + Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
}

// 把消息列表滚动到底部（等待 DOM 更新后再滚动）
async function scrollToBottom() {
  await nextTick()
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
}

// 新建会话：插入到列表最前，清空消息区
function newSession() {
  const s = { id: genId(), title: '新会话', saved: false }
  sessions.value.unshift(s)
  currentId.value = s.id
  messages.value = []
}

// 切换会话：加载对应会话的历史消息
async function switchSession(id) {
  if (id === currentId.value) return
  currentId.value = id
  await loadMessages(id)
}

// 加载指定会话的历史消息
async function loadMessages(id) {
  try {
    const res = await getMessages(id)
    messages.value = res.data || []
  } catch (e) {
    messages.value = []
  }
  await scrollToBottom()
}

// 删除会话（仅前端移除列表项）：处理删除当前会话/删空后的边界情况
async function deleteSession(id) {
  sessions.value = sessions.value.filter((s) => s.id !== id)
  if (sessions.value.length === 0) {
    newSession()                              // 全删光了就新建一个
  } else if (currentId.value === id) {
    currentId.value = sessions.value[0].id    // 删的是当前会话则切到第一个
    await loadMessages(currentId.value)
  }
}

// 确保会话已保存到后端：新会话在第一次发消息时才落库，并用首条消息做标题
async function ensureSessionSaved(session, firstText) {
  if (session.saved) return
  session.title = firstText.slice(0, 15)      // 截取前 15 字作为会话标题
  await saveSession({ id: session.id, title: session.title })
  session.saved = true
}

// 发送消息：先存会话，再推送用户消息，然后走流式接口接收 AI 回复
async function sendMessage() {
  const text = input.value.trim()
  if (!text || loading.value) return          // 空内容或已在请求中则忽略
  const session = activeSession.value
  if (!session) return

  await ensureSessionSaved(session, text)

  // 先把自己发的消息加入列表并清空输入框
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  await scrollToBottom()

  // 预置一条空的 AI 回复，后续逐字填充
  const assistant = { role: 'assistant', content: '' }
  messages.value.push(assistant)

  try {
    // 拼装流式接口地址（GET，携带会话 id 和消息内容）
    const url = `/api/ai/chat?memoryId=${encodeURIComponent(session.id)}&message=${encodeURIComponent(text)}`
    const response = await fetch(url, {
      headers: { Authorization: tokenStore.token }
    })
    // 401 表示登录过期：清空本地凭证并跳转登录页
    if (response.status === 401) {
      tokenStore.removeToken()
      userStore.removeInfo()
      router.push('/login')
      throw new Error('登录已过期')
    }
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    if (!response.body) throw new Error('浏览器不支持流式响应')

    // 读取响应流：逐个数据块解码，累加到 assistant.content 实现「打字机」效果
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')

    while (true) {
      const { done, value } = await reader.read()
      if (done) break                            // 流读完了就退出
      assistant.content += decoder.decode(value, { stream: true })
      await scrollToBottom()                     // 每收到一段就滚动到底，保持最新内容可见
    }
    assistant.content += decoder.decode()        // 流结束后冲刷剩余解码缓存
  } catch (e) {
    assistant.content = `出错了：${e.message}`    // 出错时把错误信息显示在气泡里
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

// 键盘事件：Enter 发送（不按 Shift），Shift+Enter 换行
function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

onMounted(async () => {
  // 加载历史会话列表
  try {
    const res = await getSessions()
    sessions.value = (res.data || []).map((s) => ({ ...s, saved: true }))
  } catch (e) {
    sessions.value = []
  }
  // 有历史会话就默认打开第一个，否则新建一个空会话
  if (sessions.value.length === 0) {
    newSession()
  } else {
    currentId.value = sessions.value[0].id
    await loadMessages(currentId.value)
  }
})
</script>

<style scoped lang="scss">
.ai-chat {
  display: flex;
  height: calc(100vh - 64px - 140px);
  min-height: 480px;
  max-width: 1200px;
  margin: 24px auto;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: var(--sidebar-bg);
  color: #e5e7eb;

  .brand {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 16px;
    font-size: 15px;
    font-weight: 600;
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  }

  .new-btn {
    margin: 14px 16px;
  }

  .session-list {
    flex: 1;
    overflow-y: auto;
  }

  .session-item {
    display: flex;
    align-items: center;
    padding: 11px 16px;
    cursor: pointer;
    font-size: 14px;

    &:hover {
      background: rgba(255, 255, 255, 0.06);
    }

    &.active {
      background: rgba(255, 122, 69, 0.18);
      color: #fff;
    }

    .session-title {
      flex: 1;
    }

    .del {
      cursor: pointer;
      color: #9ca3af;

      &:hover {
        color: #f87171;
      }
    }
  }
}

.chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);

  h1 {
    font-size: 17px;
    font-weight: 600;
  }

  .status {
    font-size: 12px;
    color: #18a058;
    background: #e6f7ef;
    padding: 3px 10px;
    border-radius: 999px;

    &.busy {
      color: #b7791f;
      background: #fdf3e0;
    }
  }
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty {
  margin: auto;
  text-align: center;
  color: #9aa0a6;
  font-size: 14px;
  line-height: 1.8;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 8px;
  }
}

.msg {
  display: flex;

  &.user {
    justify-content: flex-end;
  }

  .bubble {
    max-width: 78%;
    padding: 12px 16px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.7;
    white-space: pre-wrap;
    word-break: break-word;
  }

  &.user .bubble {
    background: var(--primary);
    color: #fff;
    border-bottom-right-radius: 4px;
  }

  &.assistant .bubble {
    background: #f5f6f8;
    color: var(--text);
    border: 1px solid var(--border);
    border-bottom-left-radius: 4px;
  }
}

.chat-input {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid var(--border);

  :deep(.el-textarea__inner) {
    font-family: inherit;
    line-height: 1.6;
  }
}
</style>
