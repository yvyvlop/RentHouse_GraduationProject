<template>
	<view class="page">
		<view class="user-card">
			<view class="avatar">
				<text class="avatar__text">{{ user.name ? user.name.slice(0, 1) : '未' }}</text>
			</view>
			<view class="user-card__info">
				<text class="user-card__name">{{ user.name || '未设置姓名' }}</text>
				<text class="user-card__role">{{ roleText }}</text>
			</view>
		</view>

		<view class="menu">
			<view class="menu__item" @click="goProfile">
				<text class="menu__text">编辑个人信息</text>
				<text class="menu__arrow">›</text>
			</view>
			<view class="menu__item" @click="switchRole">
				<text class="menu__text">切换到{{ user.role === 'landlord' ? '租客端' : '房东端' }}</text>
				<text class="menu__arrow">›</text>
			</view>
			<view class="menu__item" @click="logout">
				<text class="menu__text">退出登录</text>
				<text class="menu__arrow">›</text>
			</view>
		</view>

		<tab-bar current="mine"></tab-bar>
	</view>
</template>

<script>
import { getUserInfo, updateUserInfo } from '@/utils/request.js'

export default {
	data() {
		return {
			user: {
				name: '',
				role: 'tenant'
			}
		}
	},
	computed: {
		roleText() {
			return this.user.role === 'landlord' ? '房东' : '租客'
		}
	},
	onShow() {
		this.loadUserInfo()
	},
	methods: {
		async loadUserInfo() {
			try {
				const user = await getUserInfo()
				this.user = user
				// 同步到本地缓存
				uni.setStorageSync('user', user)
			} catch (err) {
				console.error('获取用户信息失败', err)
			}
		},
		goProfile() {
			uni.navigateTo({
				url: '/pages/profile/profile'
			})
		},
		// 切换身份后回主页，由主页 onShow 按新身份重定向
		async switchRole() {
			const nextRole = this.user.role === 'landlord' ? 'tenant' : 'landlord'
			uni.showLoading({
				title: '切换中'
			})
			try {
				await updateUserInfo({ role: nextRole })
				uni.hideLoading()

				// 更新本地缓存
				this.user.role = nextRole
				uni.setStorageSync('user', this.user)

				uni.reLaunch({
					url: '/pages/home/home'
				})
			} catch (err) {
				uni.hideLoading()
				console.error('切换身份失败', err)
			}
		},
		logout() {
			uni.showModal({
				title: '提示',
				content: '确定退出登录？',
				success: (res) => {
					if (!res.confirm) return
					// 清除 token 和用户信息
					uni.removeStorageSync('token')
					uni.removeStorageSync('user')
					uni.reLaunch({
						url: '/pages/login/login'
					})
				}
			})
		}
	}
}
</script>

<style scoped>
.page {
	padding: 24rpx 24rpx 140rpx;
}

.user-card {
	display: flex;
	align-items: center;
	padding: 40rpx 32rpx;
	background-color: #FFFFFF;
	border-radius: 16rpx;
}

.avatar {
	width: 112rpx;
	height: 112rpx;
	border-radius: 50%;
	background-color: #EAFBF8;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.avatar__text {
	font-size: 40rpx;
	color: #0FDC78;
}

.user-card__info {
	margin-left: 24rpx;
}

.user-card__name {
	display: block;
	font-size: 34rpx;
	font-weight: 600;
	color: #1A1A1A;
}

.user-card__role {
	display: block;
	margin-top: 8rpx;
	font-size: 24rpx;
	color: #8A8A8E;
}

.menu {
	margin-top: 24rpx;
	background-color: #FFFFFF;
	border-radius: 16rpx;
	overflow: hidden;
}

.menu__item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 104rpx;
	padding: 0 32rpx;
	border-bottom: 1rpx solid #F2F2F2;
}

.menu__item:last-child {
	border-bottom: none;
}

.menu__text {
	font-size: 30rpx;
	color: #1A1A1A;
}

.menu__arrow {
	font-size: 36rpx;
	color: #C8C8CC;
}
</style>
