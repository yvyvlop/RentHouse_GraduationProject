<template>
	<view class="page">
		<text class="title">你是租客还是房东？</text>
		<text class="desc">选择后可以在「我的」里随时切换</text>

		<view class="role-card" @click="choose('tenant')">
			<text class="role-card__title">我是租客</text>
			<text class="role-card__desc">搜索附近房源，查看房间信息和房东联系方式</text>
		</view>

		<view class="role-card" @click="choose('landlord')">
			<text class="role-card__title">我是房东</text>
			<text class="role-card__desc">发布房源，上传房间照片和信息</text>
		</view>
	</view>
</template>

<script>
import { updateUserInfo } from '@/utils/request.js'

export default {
	methods: {
		async choose(role) {
			uni.showLoading({
				title: '保存中'
			})
			try {
				// 同步到后端
				await updateUserInfo({ role })
				uni.hideLoading()

				// 更新本地缓存
				const user = uni.getStorageSync('user')
				user.role = role
				uni.setStorageSync('user', user)

				// 跳转到主页
				uni.reLaunch({
					url: '/pages/home/home'
				})
			} catch (err) {
				uni.hideLoading()
				console.error('保存身份失败', err)
			}
		}
	}
}
</script>

<style scoped>
	.page {
		padding: 60rpx 48rpx;
	}

	.title {
		display: block;
		font-size: 40rpx;
		font-weight: 600;
		color: #1A1A1A;
	}

	.desc {
		display: block;
		margin-top: 12rpx;
		font-size: 26rpx;
		color: #8A8A8E;
	}

	.role-card {
		margin-top: 40rpx;
		padding: 40rpx 32rpx;
		background-color: #FFFFFF;
		border-radius: 16rpx;
	}

	.role-card__title {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		color: #1A1A1A;
	}

	.role-card__desc {
		display: block;
		margin-top: 12rpx;
		font-size: 26rpx;
		color: #8A8A8E;
		line-height: 1.5;
	}
</style>
