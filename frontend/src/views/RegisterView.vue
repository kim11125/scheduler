<template>
  <div class="app-shell">
    <div class="reg-header">
      <button class="back-btn" @click="router.push('/login')">&#8249;</button>
      <h2>회원가입 신청</h2>
    </div>

    <div class="reg-body" v-if="!done">
      <form @submit.prevent="handleSubmit">
        <div class="field" v-for="f in fields" :key="f.key">
          <label class="field-label">{{ f.label }}</label>
          <input
            v-model="form[f.key]"
            :type="f.type"
            :placeholder="f.placeholder"
            class="field-input"
          />
          <p v-if="errors[f.key]" class="err">{{ errors[f.key] }}</p>
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const done = ref(false)
const form = reactive({ username: '', password: '', passwordConfirm: '', name: '' })
const errors = reactive<Record<string, string>>({})

const fields = [
  { key: 'username' as const,        label: '아이디',       type: 'text',     placeholder: '영문 소문자+숫자, 4~20자' },
  { key: 'password' as const,        label: '비밀번호',     type: 'password', placeholder: '영문+숫자 조합, 8자 이상' },
  { key: 'passwordConfirm' as const, label: '비밀번호 확인', type: 'password', placeholder: '비밀번호를 다시 입력하세요' },
  { key: 'name' as const,            label: '이름',         type: 'text',     placeholder: '이름을 입력하세요' },
]

function validate(): boolean {
  Object.keys(errors).forEach(k => delete (errors as Record<string,string>)[k])
  if (!/^[a-z][a-z0-9]{3,19}$/.test(form.username)) errors.username = '영문 소문자로 시작, 영문+숫자, 4~20자'
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
  padding: 16px;
  background: var(--color-status-bar);
  color: #fff;
}
.back-btn {
  font-size: 22px; color: #fff; padding: 0 4px;
}
.reg-header h2 { font-size: 17px; font-weight: 700; }
.reg-body { padding: 24px; display: flex; flex-direction: column; gap: 16px; }
form { display: flex; flex-direction: column; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 5px; }
.field-label { font-size: 13px; font-weight: 600; color: var(--color-text-secondary); }
.field-input {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1.5px solid var(--color-input-border);
  background: var(--color-input-bg);
  color: var(--color-text);
  font-size: 15px;
  outline: none;
}
.field-input:focus { border-color: var(--color-primary); }
.err { font-size: 12px; color: #F44336; }
.btn-primary {
  margin-top: 8px; padding: 14px; border-radius: 12px;
  background: var(--color-btn); color: var(--color-btn-text);
  font-size: 16px; font-weight: 700; width: 100%;
}
.done-body {
  display: flex; flex-direction: column; align-items: center;
  gap: 16px; padding: 60px 32px; text-align: center;
}
.done-icon { font-size: 56px; }
.done-body h3 { font-size: 20px; font-weight: 700; color: var(--color-text); }
.done-body p { font-size: 14px; color: var(--color-text-secondary); line-height: 1.7; }
</style>
