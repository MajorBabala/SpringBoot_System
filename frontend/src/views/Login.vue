<template>
  <div class="login-wrap">
    <div class="container">
      <div class="form-box" :class="{ shifted: isRegister }">
        <div v-show="isRegister" class="register-box">
          <h1>注册</h1>
          <input v-model="registerForm.username" type="text" placeholder="用户名" autocomplete="username" />
          <input v-model="registerForm.email" type="email" placeholder="邮箱" autocomplete="email" />
          <input v-model="registerForm.nickname" type="text" placeholder="昵称" />
          <input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            autocomplete="new-password"
          />
          <input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            autocomplete="new-password"
          />
          <span class="tips" :class="{ ok: passwordMatched, error: registerForm.confirmPassword && !passwordMatched }">
            {{ passwordTips }}
          </span>
          <select v-model="registerForm.deptId" class="dept-select">
            <option :value="null">请选择部门</option>
            <option v-for="dept in deptOptions" :key="dept.id" :value="dept.id">
              {{ dept.deptName }}
            </option>
          </select>
          <input v-model="registerForm.mobile" type="text" placeholder="手机号（选填）" />
          <button :disabled="registerLoading" @click="onRegister">
            {{ registerLoading ? '提交中...' : '立即注册' }}
          </button>
        </div>

        <div v-show="!isRegister" class="login-box">
          <h1>登录</h1>
          <input v-model="loginForm.username" type="text" placeholder="用户名" autocomplete="username" />
          <input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            autocomplete="current-password"
          />
          <button :disabled="loginLoading" @click="onLogin">
            {{ loginLoading ? '登录中...' : '立即登录' }}
          </button>
        </div>
      </div>

      <div class="con-box left">
        <h2>欢迎来到 <span>企业财务管理系统</span></h2>
        <div class="illustration">财</div>
        <p>已经有账号了？</p>
        <button @click="switchToLogin">去登录</button>
      </div>

      <div class="con-box right">
        <h2>欢迎来到 <span>企业财务管理系统</span></h2>
        <div class="illustration alt">务</div>
        <p>还没有员工账号？</p>
        <button @click="switchToRegister">去注册</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listDepts } from '../api/depts'
import { login, register } from '../api/auth'

const router = useRouter()

const isRegister = ref(false)
const loginLoading = ref(false)
const registerLoading = ref(false)
const deptOptions = ref([])

const loginForm = reactive({
  username: '',
  password: ''
})

const createRegisterForm = () => ({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  deptId: null,
  email: '',
  mobile: ''
})

const registerForm = reactive(createRegisterForm())

const passwordMatched = computed(
  () => !!registerForm.password && registerForm.password === registerForm.confirmPassword
)

const passwordTips = computed(() => {
  if (!registerForm.confirmPassword) return ''
  return passwordMatched.value ? '两次密码输入一致' : '两次密码输入不一致'
})

const fetchDepts = async () => {
  try {
    const resp = await listDepts()
    if (resp.code === 200) {
      deptOptions.value = resp.data || []
      if (!registerForm.deptId && deptOptions.value.length > 0) {
        registerForm.deptId = deptOptions.value[0].id
      }
      return true
    }
    ElMessage.error(resp.message || '部门列表加载失败')
    return false
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '部门列表加载失败')
    return false
  }
}

const resetRegisterForm = () => {
  Object.assign(registerForm, createRegisterForm())
  if (deptOptions.value.length > 0) {
    registerForm.deptId = deptOptions.value[0].id
  }
}

const switchToRegister = async () => {
  if (deptOptions.value.length === 0) {
    const ok = await fetchDepts()
    if (!ok) return
  }
  resetRegisterForm()
  isRegister.value = true
}

const switchToLogin = () => {
  isRegister.value = false
  loginForm.password = ''
}

const onLogin = async () => {
  if (!loginForm.username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }
  if (!loginForm.password) {
    ElMessage.error('请输入密码')
    return
  }

  loginLoading.value = true
  try {
    const resp = await login(loginForm.username.trim(), loginForm.password)
    if (resp.code === 200 && resp.data?.token) {
      const finalUsername = (resp.data.username || loginForm.username || '').trim()
      localStorage.setItem('token', resp.data.token)
      localStorage.setItem('roles', JSON.stringify(resp.data.roles || []))
      if (finalUsername) {
        localStorage.setItem('username', finalUsername)
      }
      ElMessage.success('登录成功')
      router.push('/home/dashboard')
      return
    }
    ElMessage.error(resp.message || '登录失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '登录失败')
  } finally {
    loginLoading.value = false
  }
}

const onRegister = async () => {
  if (!registerForm.username.trim()) {
    ElMessage.error('请输入用户名')
    return
  }
  if (!registerForm.nickname.trim()) {
    ElMessage.error('请输入昵称')
    return
  }
  if (!registerForm.password) {
    ElMessage.error('请输入密码')
    return
  }
  if (registerForm.password.length < 6 || registerForm.password.length > 20) {
    ElMessage.error('密码长度需在 6 到 20 位之间')
    return
  }
  if (!registerForm.confirmPassword) {
    ElMessage.error('请输入确认密码')
    return
  }
  if (!passwordMatched.value) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (!registerForm.deptId) {
    ElMessage.error('请选择部门')
    return
  }
  if (registerForm.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
    ElMessage.error('邮箱格式不正确')
    return
  }
  if (registerForm.mobile && !/^\d{11}$/.test(registerForm.mobile)) {
    ElMessage.error('手机号必须为 11 位数字')
    return
  }

  registerLoading.value = true
  try {
    const resp = await register({
      ...registerForm,
      username: registerForm.username.trim(),
      nickname: registerForm.nickname.trim(),
      email: registerForm.email.trim(),
      mobile: registerForm.mobile.trim()
    })
    if (resp.code === 200) {
      ElMessage.success(`注册成功，默认角色：${resp.data?.roleCode || 'EMPLOYEE'}`)
      loginForm.username = registerForm.username
      loginForm.password = ''
      isRegister.value = false
      resetRegisterForm()
      return
    }
    ElMessage.error(resp.message || '注册失败')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '注册失败')
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-wrap {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  background: linear-gradient(250deg, #020617, #f8fafc);
}

.container {
  background-color: #fff;
  width: min(920px, 100%);
  min-height: 560px;
  border-radius: 50px;
  box-shadow: 10px 10px 30px rgba(15, 23, 42, 0.28);
  position: relative;
  overflow: hidden;
}

.form-box {
  position: absolute;
  top: -10%;
  left: 5%;
  background-color: #020617;
  width: min(380px, 90%);
  min-height: 650px;
  border-radius: 8px;
  box-shadow: 2px 0 18px rgba(0, 0, 0, 0.12);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2;
  transition: transform 0.8s ease-in-out;
  padding: 34px 0;
}

.form-box.shifted {
  transform: translateX(80%);
}

.register-box,
.login-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

h1 {
  text-align: center;
  margin-bottom: 25px;
  text-transform: uppercase;
  color: #fff;
  letter-spacing: 5px;
}

input,
.dept-select {
  background-color: transparent;
  width: 70%;
  color: #fff;
  border: none;
  border-bottom: 1px solid rgba(255, 255, 255, 0.9);
  padding: 10px 0;
  text-indent: 10px;
  margin: 8px 0;
  font-size: 14px;
  letter-spacing: 2px;
}

input::placeholder {
  color: rgba(255, 255, 255, 0.9);
}

input:focus,
.dept-select:focus {
  color: #fef08a;
  outline: none;
  border-bottom: 3px solid #7f1d1d;
  transition: 0.5s;
}

input:focus::placeholder {
  opacity: 0;
}

.dept-select option {
  background: #020617;
  color: #fff;
}

.tips {
  width: 70%;
  min-height: 18px;
  margin-top: 2px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.tips.ok {
  color: #86efac;
}

.tips.error {
  color: #fca5a5;
}

.form-box button {
  width: 70%;
  margin-top: 28px;
  background-color: #fff;
  outline: none;
  border-radius: 8px;
  padding: 13px;
  color: #000;
  letter-spacing: 2px;
  border: none;
  cursor: pointer;
  font-weight: 700;
}

.form-box button:hover:not(:disabled) {
  background-color: #15803d;
  color: #fff;
  transition: background-color 0.5s ease;
}

.form-box button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.con-box {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
}

.con-box.left {
  left: -2%;
}

.con-box.right {
  right: -2%;
}

.con-box h2 {
  color: #8e9aaf;
  font-size: 25px;
  font-weight: 700;
  letter-spacing: 3px;
  text-align: center;
  margin-bottom: 4px;
}

.con-box p {
  font-size: 12px;
  letter-spacing: 2px;
  color: #8e9aaf;
  text-align: center;
}

.con-box span {
  color: #0f172a;
}

.illustration {
  width: 150px;
  height: 150px;
  opacity: 0.92;
  margin: 40px 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #111827, #1d4ed8);
  color: #fff;
  font-size: 72px;
  font-weight: 800;
}

.illustration.alt {
  background: linear-gradient(135deg, #0f766e, #22c55e);
}

.con-box button {
  margin-top: 3%;
  background-color: #fff;
  color: #111827;
  border: 1px solid #94a3b8;
  padding: 6px 10px;
  border-radius: 5px;
  letter-spacing: 1px;
  outline: none;
  cursor: pointer;
}

.con-box button:hover {
  background-color: #15803d;
  color: #fff;
}

@media (max-width: 960px) {
  .container {
    min-height: 760px;
    border-radius: 28px;
  }

  .form-box {
    position: relative;
    top: auto;
    left: auto;
    width: 100%;
    min-height: auto;
    padding: 40px 0;
    border-radius: 0;
    transform: none !important;
  }

  .con-box {
    position: static;
    width: 100%;
    transform: none;
    padding: 24px 16px;
  }

  .con-box.left,
  .con-box.right {
    left: auto;
    right: auto;
  }

  .container {
    display: flex;
    flex-direction: column;
  }
}
</style>
