<template>
  <div class="app-shell">
    <div class="reg-header">
      <button class="back-btn" @click="router.push('/login')">&#8249;</button>
      <h2>회원가입 신청</h2>
    </div>

    <div class="reg-body" v-if="!done">
      <form @submit.prevent="handleSubmit">
        <!-- 아이디 + 중복 확인 -->
        <div class="field">
          <label class="field-label">아이디</label>
          <div class="id-row">
            <input
              v-model="form.username"
              type="text"
              placeholder="영문 소문자+숫자, 4~20자"
              class="field-input id-input"
              @input="onUsernameInput"
            />
            <button
              type="button"
              class="btn-check"
              :disabled="!canCheck || checkLoading"
              @click="checkDuplicate"
            >{{ checkLoading ? '확인 중' : '중복 확인' }}</button>
          </div>
          <p v-if="errors.username" class="err">{{ errors.username }}</p>
          <p v-if="idCheckResult === 'available'" class="msg-ok">✓ 사용 가능한 아이디입니다.</p>
          <p v-if="idCheckResult === 'taken'" class="msg-err">✗ 이미 사용 중인 아이디입니다.</p>
        </div>

        <!-- 비밀번호 -->
        <div class="field">
          <label class="field-label">비밀번호</label>
          <input v-model="form.password" type="password" placeholder="영문+숫자 조합, 8자 이상" class="field-input" />
          <p v-if="errors.password" class="err">{{ errors.password }}</p>
        </div>

        <!-- 비밀번호 확인 -->
        <div class="field">
          <label class="field-label">비밀번호 확인</label>
          <input v-model="form.passwordConfirm" type="password" placeholder="비밀번호를 다시 입력하세요" class="field-input" />
          <p v-if="errors.passwordConfirm" class="err">{{ errors.passwordConfirm }}</p>
        </div>

        <!-- 이름 -->
        <div class="field">
          <label class="field-label">이름</label>
          <input v-model="form.name" type="text" placeholder="이름을 입력하세요" class="field-input" />
          <p v-if="errors.name" class="err">{{ errors.name }}</p>
        </div>

        <button type="submit" class="btn-primary">가입 신청</button>
      </form>
    </div>

    <div class="done-body" v-else>
      <div class="done-icon">✅</div>
      <h3>신청이 완료되었습니다</h3>
      <p>관리자 승인을 기다려주세요.<br />승인 후 로그인할 수 있습니다.</p>
      <button class="btn-primary" @click="router.push('/login')">로그인으로 이동</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()
const done = ref(false)
const form = reactive({ username: '', password: '', passwordConfirm: '', name: '' })
const errors = reactive<Record<string, string>>({})

// 아이디 중복 확인 상태
const idCheckResult = ref<'none' | 'available' | 'taken'>('none')
const checkLoading = ref(false)

const canCheck = computed(() => /^[a-z][a-z0-9]{3,19}$/.test(form.username))

function onUsernameInput() {
  idCheckResult.value = 'none'
  delete (errors as Record<string, string>).username
}

async function checkDuplicate() {
  if (!canCheck.value) return
  checkLoading.value = true
  try {
    const res = await authApi.checkLoginId(form.username)
    idCheckResult.value = res.data.available ? 'available' : 'taken'
  } catch {
    idCheckResult.value = 'none'
  } finally {
    checkLoading.value = false
  }
}

function validate(): boolean {
  Object.keys(errors).forEach(k => delete (errors as Record<string, string>)[k])
  if (!/^[a-z][a-z0-9]{3,19}$/.test(form.username)) {
    errors.username = '영문 소문자로 시작, 영문+숫자, 4~20자'
  } else if (idCheckResult.value === 'none') {
    errors.username = '아이디 중복 확인을 해주세요.'
  } else if (idCheckResult.value === 'taken') {
    errors.username = '이미 사용 중인 아이디입니다.'
  }
  if (!/^(?=.*[a-zA-Z])(?=.*\d).{8,50}$/.test(form.password)) errors.password = '영문+숫자 조합, 8자 이상'
  if (form.password !== form.passwordConfirm) errors.passwordConfirm = '비밀번호가 일치하지 않습니다'
  if (!form.name.trim()) errors.name = '이름을 입력하세요'
  return Object.keys(errors).length === 0
}

async function handleSubmit() {
  if (!validate()) return
  const ok = await authStore.register(form.username, form.password, form.name)
  if (ok) {
    done.value = true
  } else {
    errors.username = authStore.error || '회원가입에 실패했습니다.'
  }
}
</script>

<style scoped>
.reg-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  height: 56px;
  background: var(--color-header-bg);
  color: var(--color-header-text);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}
.back-btn {
  font-size: 26px; color: var(--color-header-text); padding: 0 4px; line-height: 1;
}
.reg-header h2 { font-size: 17px; font-weight: 700; }

.reg-body {
  padding: 20px 20px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

form { display: flex; flex-direction: column; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field-label {
  font-size: 12px; font-weight: 600;
  color: var(--color-text-secondary);
  letter-spacing: 0.2px;
}
.field-input {
  padding: 13px 16px;
  border-radius: var(--radius-md);
  border: 1.5px solid var(--color-border);
  background: var(--color-surface-muted);
  color: var(--color-text-primary);
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  width: 100%;
}
.field-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
  background: var(--color-surface);
}

/* 아이디 행 */
.id-row { display: flex; gap: 8px; }
.id-input { flex: 1; }
.btn-check {
  padding: 0 16px;
  border-radius: var(--radius-pill);
  background: var(--color-primary);
  color: var(--color-btn-text);
  font-size: 12px; font-weight: 700;
  flex-shrink: 0;
  white-space: nowrap;
  transition: opacity 0.2s;
  border: none;
  cursor: pointer;
}
.btn-check:disabled { opacity: 0.4; cursor: not-allowed; }

.err { font-size: 12px; color: var(--color-danger); }
.msg-ok { font-size: 12px; color: var(--color-success); font-weight: 600; }
.msg-err { font-size: 12px; color: var(--color-danger); }

.btn-primary {
  margin-top: 8px; padding: 15px; border-radius: var(--radius-pill);
  background: var(--color-btn); color: var(--color-btn-text);
  font-size: 15px; font-weight: 700; width: 100%;
  transition: opacity 0.2s, transform 0.15s;
  box-shadow: var(--shadow-soft);
}
.btn-primary:active { opacity: 0.85; transform: scale(0.98); }

.done-body {
  display: flex; flex-direction: column; align-items: center;
  gap: 18px; padding: 60px 32px; text-align: center;
}
.done-icon { font-size: 64px; }
.done-body h3 { font-size: 22px; font-weight: 700; color: var(--color-text-primary); }
.done-body p { font-size: 14px; color: var(--color-text-secondary); line-height: 1.7; }
</style>
