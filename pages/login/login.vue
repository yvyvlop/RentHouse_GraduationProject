<template>
	<view class="page">
		<view class="brand">
			<text class="brand__title">租房管理系统</text>
			<text class="brand__desc">找房 / 发布房源</text>
		</view>

		<view class="login-btn" @click="handleLogin">
			<text class="login-btn__text">微信一键登录</text>
		</view>
	</view>
</template>

<script>
import { login } from '@/utils/request.js'

export default {
	methods: {
		handleLogin() {
			uni.showLoading({
				title: '登录中'
			})
			uni.login({
				provider: 'weixin',
				success: (res) => {
					// 调用后端登录接口
					this.doLogin(res.code)
				},
				fail: () => {
					uni.hideLoading()
					// 开发阶段：微信登录不可用时，允许以体验模式进入
					uni.showModal({
						title: '微信登录不可用',
						content: '是否使用体验模式登录？',
						confirmText: '体验模式',
						success: (modalRes) => {
							if (modalRes.confirm) {
								// 体验模式：用固定 code 调后端
								this.doLogin('dev-test')
							}
						}
					})
				}
			})
		},

		async doLogin(code) {
			try {
				// 调后端登录接口
				const data = await login(code)
				uni.hideLoading()

				// 保存 token 和用户信息
				uni.setStorageSync('token', data.token)
				uni.setStorageSync('user', data.user)

				// 跳转到身份选择或主页
				const role = data.user.role
				if (role && role !== 'tenant') {
					// 已有身份且不是默认租客，直接进主页
					uni.reLaunch({
						url: '/pages/home/home'
					})
				} else {
					// 新用户或租客，去选择身份
					uni.reLaunch({
						url: '/pages/role/role'
					})
				}
			} catch (err) {
				uni.hideLoading()
				console.error('登录失败', err)
			}
		}
	}
}
</script>

<style scoped>
.page {
	min-height: 100vh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 0 60rpx;
	background-color: #FFFFFF;
}

.brand__title {
	display: block;
	font-size: 48rpx;
	font-weight: 600;
	color: #1A1A1A;
	text-align: center;
}

.brand__desc {
	display: block;
	margin-top: 16rpx;
	font-size: 26rpx;
	color: #8A8A8E;
	text-align: center;
}

.login-btn {
	margin-top: 120rpx;
	width: 100%;
	height: 92rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #0FDC78;
	border-radius: 999rpx;
}

.login-btn__text {
	font-size: 32rpx;
	color: #004D26;
}
</style>
