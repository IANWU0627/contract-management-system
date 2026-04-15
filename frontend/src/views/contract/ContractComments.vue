<template>
  <div class="comments">
    <div class="comment-input">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        :placeholder="t('comment.placeholder')"
      />
      <el-button type="primary" @click="handleAddComment" :loading="loading" style="margin-top: 10px">
        {{ t('comment.submit') }}
      </el-button>
    </div>
    
    <el-divider />
    
    <div class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="36">{{ comment.userName?.[0] }}</el-avatar>
        <div class="comment-content">
          <div class="comment-header">
            <span class="username">{{ comment.userName }}</span>
            <span class="time">{{ formatTime(comment.createdAt) }}</span>
          </div>
          <div class="comment-text">{{ comment.content }}</div>
          <div class="comment-actions">
            <el-button type="primary" text size="small" @click="handleReply(comment)">
              <el-icon><ChatDotRound /></el-icon>
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(comment.id)" v-if="comment.userId === currentUserId">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      
      <el-empty v-if="comments.length === 0" :description="t('comment.noComments')" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getComments, addComment, deleteComment } from '@/api/extra'
import { useUserStore } from '@/stores/user'
import { ChatDotRound, Delete } from '@element-plus/icons-vue'

const { t, locale } = useI18n()

const route = useRoute()
const props = defineProps<{ contractId?: number }>()

// 优先使用 props，否则从路由获取
const contractId = computed(() => props.contractId || Number(route.params.id))

const userStore = useUserStore()
const currentUserId = userStore.userInfo?.id

const comments = ref<any[]>([])
const newComment = ref('')
const loading = ref(false)

const fetchComments = async () => {
  try {
    const res = await getComments(contractId.value)
    // 后端返回 { total, list } 格式
    comments.value = res.data?.list || res.data || []
  } catch (error) { }
}

const handleAddComment = async () => {
  if (!newComment.value.trim()) return
  loading.value = true
  try {
    await addComment(contractId.value, { content: newComment.value })
    newComment.value = ''
    ElMessage.success(t('common.success'))
    fetchComments()
  } catch (error) {
    ElMessage.error(t('common.error'))
  } finally {
    loading.value = false
  }
}

const handleReply = (comment: any) => {
  newComment.value = `@${comment.userName} `
}

const handleDelete = async (commentId: number) => {
  try {
    await deleteComment(contractId.value, commentId)
    ElMessage.success(t('common.success'))
    fetchComments()
  } catch (error) {
    ElMessage.error(t('common.error'))
  }
}

const formatTime = (time: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString(locale.value === 'en' ? 'en-US' : 'zh-CN')
}

onMounted(() => { fetchComments() })
</script>

<style scoped lang="scss">
.comments {
  .comment-input {
    margin-bottom: 20px;
  }
  
  .comment-item {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    
    .comment-content {
      flex: 1;
      
      .comment-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 6px;
        
        .username {
          font-weight: 500;
          color: var(--text-primary);
        }
        
        .time {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
      
      .comment-text {
        color: var(--text-regular);
        line-height: 1.6;
        margin-bottom: 8px;
      }
      
      .comment-actions {
        font-size: 12px;
      }
    }
  }
}
</style>
