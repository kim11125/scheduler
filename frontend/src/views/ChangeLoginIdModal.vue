<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-sheet" @click.stop>
        <div class="modal-handle"></div>
        <div class="modal-header">
          <h3>로그인 아이디 변경</h3>
          <button class="modal-close" @click="$emit('close')">✕</button>
        </div>

        <div class="modal-body">
          <div class="form-field">
            <label class="form-label">현재 비밀번호 <span class="required">*</span></label>
            <input
              v-model="form.currentPassword"
              type="password"
              class="form-input"
              placeholder="현재 비밀번호를 입력하세요"
              autocomplete="current-password"
            />
          </div>

          <div class="form-field">
            <label class="form-label">새 로그인 아이디 <span class="required">*</span></label>
            <div class="id-row">
              <input
                v-model="form.newLoginId"
                type="text"
                class="form-input id-input"
                placeholder="영문 소문자+숫자, 4~20자"
                @input="onIdInput"
              />
              <button
                type="button"
                class="btn-check"
                :disabled="!canCheck || checkLoading"
                @click="checkDuplicate"
              >{{ checkLoading ? '확인 중' : '중복 확인' }}</button>
            </div>
            <p v-if="idCheckResult === 'available'" class="msg-ok">✓ 사용 가능한 아이디입니다.</p>
            <p v-if="idCheckResult === 'taken'" class="msg-err">✗ 이미 사용 중인 아이디입니다.</p>
          </div>

          <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

          <div class="modal-actions">
            <button type="button" class="btn-cancel" @click="$emit('close')">취소</button>
            <button type="button" class="btn-save" :disabled="saving" @click="handleSubmit">
              {{ saving ? '변경 중...' : '변경' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'

const emit = defineEmits<{ close: [] }>()
const router = useRouter()
const authStore = useAuthStore()

const form = reactive({ currentPassword: '', newLoginId: '' })
const idCheckResult = ref<'none' | 'available' | 'taken'>('none')
const checkLoading = ref(false)
const saving = ref(false)
const errorMsg = ref('')

const canCheck = computed(() => /^[a-z][a-z0-9]{3,19}$/.test(form.newLoginId))

function onIdInput() {
  idCheckResult.value = 'none'
  errorMsg.value = ''
}

async function checkDuplicate() {
  if (!canCheck.value) return
  checkLoading.value = true
  try {
    const res = await authApi.checkLoginId(form.newLoginId)
    idCheckResult.value = res.data.available ? 'available' : 'taken'
  } catch {
    idCheckResult.value = 'none'
  } finally {
    checkLoading.value = false
  }
}

async function handleSubmit() {
  errorMsg.value = ''
  if (!form.currentPassword) { errorMsg.value = '현재 비밀번호를 입력하세요.'; return }
  if (!canCheck.value) { errorMsg.value = '아이디 형식이 올바르지 않습니다.'; return }
  if (idCheckResult.value === 'none') { errorMsg.value = '아이디 중복 확인을 해주세요.'; return }
  if (idCheckResult.value === 'taken') { errorMsg.value = '이미 사용 중인 아이디입니다.'; return }

  saving.value = true
  try {
    const res = await userApi.changeMyLoginId({ currentPassword: form.currentPassword, newLoginId: form.newLoginId })
    const data = res.data
    if (data.forceLogout || data.loginIdChanged) {
      // 로컬 데이터 초기화 후 로그인 화면으로
      localStorage.removeItem('token')
      localStorage.removeItem('auth_user')
      authStore.user = null
      router.push('/login?reason=login-id-changed')
    } else {
      emit('close')
    }
  } catch (e: any) {
    errorMsg.value = e.response?.data?.message || '아이디 변경에 실패했습니다.'
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: var(--color-overlay);
  display: flex; align-items: flex-end; justify-content: center;
  z-index: 600;
}
.modal-sheet {
  width: 100%; max-width: 430px; max-height: 90vh;
  background: var(--color-card);
  border-radius: 20px 20px 0 0;
  overflow-y: auto;
  animation: slideUp 0.25s ease;
}
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-handle {
  width: 40px; height: 4px;
  background: var(--color-separator); border-radius: 2px;
  margin: 10px auto 0;
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px 12px;
  border-bottom: 1px solid var(--color-separator);
}
.modal-header h3 { font-size: 17px; font-weight: 700; color: var(--color-text); }
.modal-close { font-size: 18px; color: var(--color-text-secondary); padding: 4px 8px; }

.modal-body {
  padding: 20px;
  display: flex; flex-direction: column; gap: 18px;
}
.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); }
.required { color: #F44336; }
.form-input {
  padding: 11px 14px; border-radius: 10px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg); color: var(--color-text);
  font-size: 15px; outline: none; width: 100%;
  transition: border-color 0.2s;
}
.form-input:focus { border-color: var(--color-primary); }

.id-row { display: flex; gap: 8px; }
.id-input { flex: 1; }
.btn-check {
  padding: 0 14px; border-radius: 10px;
  background: var(--color-primary); color: var(--color-on-primary);
  font-size: 13px; font-weight: 700; flex-shrink: 0; white-space: nowrap;
  transition: opacity 0.2s;
}
.btn-check:disabled { opacity: 0.45; cursor: not-allowed; }

.msg-ok  { font-size: 12px; color: #2E7D32; }
.msg-err { font-size: 12px; color: #F44336; }
.error-msg { font-size: 13px; color: #F44336; }

.modal-actions {
  display: flex; justify-content: flex-end; gap: 8px; padding-top: 4px;
}
.btn-cancel {
  padding: 11px 18px; border-radius: 10px;
  background: var(--color-surface); color: var(--color-text-secondary);
  font-size: 14px; font-weight: 600;
  border: 1px solid var(--color-separator);
}
.btn-save {
  padding: 11px 24px; border-radius: 10px;
  background: var(--color-btn); color: var(--color-btn-text);
  font-size: 14px; font-weight: 700;
  transition: opacity 0.2s;
}
.btn-save:disabled { opacity: 0.5; }
</style>
