<template>
	<view class="page">
		<view class="head">
			<text class="head__title">房东中心</text>
			<text class="head__desc">管理你发布的房源</text>
		</view>

		<view class="entry" @click="goPublish">
			<text class="entry__title">发布房源</text>
			<text class="entry__desc">填写详细地址、房间大小格局、租金，上传房间真实图片</text>
		</view>

		<view class="entry" @click="goMyList">
			<text class="entry__title">我发布的房源</text>
			<text class="entry__desc">查看、编辑、下架已发布的房源</text>
		</view>

		<tab-bar current="home"></tab-bar>
	</view>
</template>

<script>
	export default {
		onShow() {
			if (!uni.getStorageSync('openid')) {
				uni.reLaunch({
					url: '/pages/login/login'
				})
				return
			}
			// 身份分流：租客访问房东主页时重定向回租客主页
			if (uni.getStorageSync('role') !== 'landlord') {
				uni.reLaunch({
					url: '/pages/home/home'
				})
			}
		},
		methods: {
			goPublish() {
				uni.navigateTo({
					url: '/pages/landlord/publish/publish'
				})
			},
			goMyList() {
				uni.navigateTo({
					url: '/pages/landlord/list/list'
				})
			}
		}
	}
</script>

<style scoped>
	.page {
		padding: 24rpx 24rpx 140rpx;
	}

	.head {
		padding: 24rpx 0 40rpx;
	}

	.head__title {
		display: block;
		font-size: 44rpx;
		font-weight: 600;
		color: #1A1A1A;
	}

	.head__desc {
		display: block;
		margin-top: 12rpx;
		font-size: 26rpx;
		color: #8A8A8E;
	}

	.entry {
		margin-bottom: 24rpx;
		padding: 40rpx 32rpx;
		background-color: #FFFFFF;
		border-radius: 16rpx;
	}

	.entry__title {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		color: #1A1A1A;
	}

	.entry__desc {
		display: block;
		margin-top: 12rpx;
		font-size: 26rpx;
		color: #8A8A8E;
		line-height: 1.5;
	}
</style>