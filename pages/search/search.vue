<template>
	<view class="page">
		<view class="search-row">
			<input class="search-input" v-model="keyword" placeholder="输入小区、写字楼或地址" confirm-type="search"
				@confirm="doSearch" />
			<view class="choose-btn" @click="chooseAddress">
				<text class="choose-btn__text">选地址</text>
			</view>
		</view>

		<view class="tip" v-if="address">
			<text class="tip__text">搜索位置：{{ address }}</text>
		</view>

		<view class="house-card" v-for="item in houses" :key="item.id">
			<image class="house-card__cover" :src="item.cover" mode="aspectFill"></image>
			<view class="house-card__body">
				<text class="house-card__title">{{ item.title }}</text>
				<text class="house-card__meta">{{ item.area }}㎡ · {{ item.layout }}</text>
				<text class="house-card__meta">{{ item.address }}</text>
				<text class="house-card__distance" v-if="item.distance != null">距您约 {{ item.distance.toFixed(1) }}
					km</text>
				<view class="house-card__foot">
					<text class="house-card__price">{{ item.price }} 元/月</text>
					<detail-btn :house-id="item.id" size="small"></detail-btn>
				</view>
			</view>
		</view>

		<view class="empty" v-if="searched && !houses.length">
			<text class="empty__text">没有找到相关房源</text>
		</view>
	</view>
</template>

<script>
import { searchHouses, preloadCovers } from '@/utils/request.js'

export default {
	data() {
		return {
			keyword: '',
			address: '',
			lat: 0,
			lng: 0,
			searched: false,
			houses: []
		}
	},
	methods: {
		async doSearch() {
			if (!this.keyword && !this.address) {
				uni.showToast({
					title: '请输入地址或选择位置',
					icon: 'none'
				})
				return
			}
			try {
				// 关键词 + 经纬度（有位置时按距离排序）
				const list = await searchHouses(this.keyword, this.lat, this.lng)
				// 预下载封面图到本地（真机上 image 直接加载 HTTP 图片会被拦截）
				this.houses = await preloadCovers(list)
				this.searched = true
			} catch (err) {
				console.error('搜索失败', err)
			}
		},
		chooseAddress() {
			uni.chooseLocation({
				success: (res) => {
					this.address = res.name || res.address
					this.lat = res.latitude
					this.lng = res.longitude
					this.doSearch()
				}
			})
		}
	}
}
</script>

<style scoped>
.page {
	padding: 24rpx;
}

.search-row {
	display: flex;
	align-items: center;
}

.search-input {
	flex: 1;
	height: 76rpx;
	padding: 0 24rpx;
	font-size: 28rpx;
	background-color: #FFFFFF;
	border-radius: 999rpx;
}

.choose-btn {
	margin-left: 16rpx;
	height: 76rpx;
	padding: 0 28rpx;
	display: flex;
	align-items: center;
	background-color: #0FDC78;
	border-radius: 999rpx;
}

.choose-btn__text {
	font-size: 26rpx;
	color: #004D26;
}

.tip {
	margin-top: 16rpx;
}

.tip__text {
	font-size: 24rpx;
	color: #8A8A8E;
}

.house-card {
	display: flex;
	margin-top: 24rpx;
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
	margin-top: 12rpx;
	font-size: 24rpx;
	color: #8A8A8E;
}

.house-card__distance {
	margin-top: 8rpx;
	font-size: 22rpx;
	color: #0FDC78;
}

.house-card__foot {
	margin-top: auto;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.house-card__price {
	font-size: 32rpx;
	font-weight: 600;
	color: #FF5B3B;
}

.empty {
	padding: 80rpx 0;
	text-align: center;
}

.empty__text {
	font-size: 26rpx;
	color: #8A8A8E;
}
</style>