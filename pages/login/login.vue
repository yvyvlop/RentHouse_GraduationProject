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
export default {
	methods: {
		handleLogin() {
			uni.showLoading({
				title: '登录中'
			})
			uni.login({
				provider: 'weixin',
				success: (res) => {
					uni.hideLoading()
					// TODO 把 res.code 发给后端，由后端用 appid + AppSecret 换取 openid
					this.afterLogin(res.code)
				},
				fail: () => {
					uni.hideLoading()
					// 开发阶段：未配置小程序 appid 或后端服务时，允许以体验模式进入
					uni.showModal({
						title: '微信登录不可用',
						content: '当前未配置小程序 appid 或后端服务，是否以体验模式进入？',
						confirmText: '体验模式',
						success: (res) => {
							if (res.confirm) this.afterLogin('dev-openid')
						}
					})
				}
			})
		},
		afterLogin(openid) {
			uni.setStorageSync('openid', openid || 'dev-openid')
			const role = uni.getStorageSync('role')
			uni.reLaunch({
				url: role ? '/pages/home/home' : '/pages/role/role'
			})
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