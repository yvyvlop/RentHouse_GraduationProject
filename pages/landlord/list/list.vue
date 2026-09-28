<template>
	<view class="page">
		<view class="house-card" v-for="item in houses" :key="item.id">
			<image class="house-card__cover" :src="item.cover" mode="aspectFill"></image>
			<view class="house-card__body">
				<text class="house-card__title">{{ item.title }}</text>
				<text class="house-card__meta">{{ item.area }}㎡ · {{ item.layout }}</text>
				<text class="house-card__meta">{{ item.price }} 元/月</text>

				<view class="house-card__actions">
					<view class="action" @click="goEdit(item)">
						<text class="action__text">编辑</text>
					</view>
					<view class="action" @click="remove(item)">
						<text class="action__text">删除</text>
					</view>
					<detail-btn :house-id="item.id" size="small"></detail-btn>
				</view>
			</view>
		</view>

		<view class="empty" v-if="!houses.length">
			<text class="empty__text">还没有发布房源</text>
		</view>

		<view class="publish-btn" @click="goPublish">
			<text class="publish-btn__text">发布新房源</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				houses: []
			}
		},
		onShow() {
			// 发布完返回时会重新拉取列表
			this.loadList()
		},
		methods: {
			loadList() {
				// TODO 接后端接口：只查当前登录房东发布的房源
				this.houses = [{
					id: 1,
					cover: '/static/logo.png',
					title: '示例房源 · 阳光单间',
					area: 25,
					layout: '1室1厅1卫',
					price: 1500
				}]
			},
			goEdit(item) {
				uni.navigateTo({
					url: `/pages/landlord/publish/publish?id=${item.id}`
				})
			},
			goPublish() {
				uni.navigateTo({
					url: '/pages/landlord/publish/publish'
				})
			},
			remove(item) {
				uni.showModal({
					title: '提示',
					content: `确定删除「${item.title}」？`,
					success: (res) => {
						if (!res.confirm) return
						// TODO 接后端删除接口
						this.houses = this.houses.filter(h => h.id !== item.id)
						uni.showToast({
							title: '已删除'
						})
					}
				})
			}
		}
	}
</script>

<style scoped>
	.page {
		padding: 24rpx 24rpx 60rpx;
	}

	.house-card {
		display: flex;
		margin-bottom: 24rpx;
		padding: 16rpx;
		background-color: #FFFFFF;
		border-radius: 16rpx;
	}

	.house-card__cover {
		width: 200rpx;
		height: 200rpx;
		border-radius: 12rpx;
		background-color: #F2F2F2;
		flex-shrink: 0;
	}

	.house-card__body {
		flex: 1;
		margin-left: 20rpx;
		display: flex;
		flex-direction: column;
	}

	.house-card__title {
		font-size: 30rpx;
		font-weight: 600;
		color: #1A1A1A;
	}

	.house-card__meta {
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #8A8A8E;
	}

	.house-card__actions {
		margin-top: auto;
		display: flex;
		align-items: center;
		justify-content: flex-end;
	}

	.action {
		height: 52rpx;
		padding: 0 24rpx;
		margin-right: 16rpx;
		display: flex;
		align-items: center;
		border: 1rpx solid #E5E5E7;
		border-radius: 999rpx;
	}

	.action__text {
		font-size: 24rpx;
		color: #1A1A1A;
	}

	.empty {
		padding: 80rpx 0;
		text-align: center;
	}

	.empty__text {
		font-size: 26rpx;
		color: #8A8A8E;
	}

	.publish-btn {
		margin-top: 40rpx;
		height: 92rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #0FDC78;
		border-radius: 999rpx;
	}

	.publish-btn__text {
		font-size: 32rpx;
		color: #004D26;
	}
</style>