<template>
	<view class="page">
		<!-- 搜索栏 + 定位 -->
		<view class="search-row">
			<view class="search-box" @click="goSearch">
				<text class="search-box__placeholder">搜索地址，如“XX小区”</text>
			</view>
			<view class="locate-btn" @click="autoLocate">
				<text class="locate-btn__text">定位</text>
			</view>
		</view>

		<view class="current-loc" v-if="currentAddress">
			<text class="current-loc__text">当前位置：{{ currentAddress }}</text>
		</view>

		<!-- 附近房源 -->
		<view class="section-title">
			<text class="section-title__text">附近房源</text>
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

		<view class="empty" v-if="!houses.length">
			<text class="empty__text">附近暂无房源</text>
		</view>

		<tab-bar current="home"></tab-bar>
	</view>
</template>

<script>
import { getNearbyHouses, preloadCovers } from '@/utils/request.js'

export default {
	data() {
		return {
			lat: 0,
			lng: 0,
			currentAddress: '',
			houses: []
		}
	},
	onShow() {
		const token = uni.getStorageSync('token')
		if (!token) {
			uni.reLaunch({
				url: '/pages/login/login'
			})
			return
		}
		// 身份分流：房东访问租客主页时重定向到房东主页
		const user = uni.getStorageSync('user')
		if (user && user.role === 'landlord') {
			uni.reLaunch({
				url: '/pages/landlord/home/home'
			})
			return
		}
		if (!this.houses.length) {
			this.autoLocate()
		}
	},
	methods: {
		goSearch() {
			uni.navigateTo({
				url: '/pages/search/search'
			})
		},
		// 自动定位
		autoLocate() {
			uni.getLocation({
				type: 'gcj02',
				success: (res) => {
					this.lat = res.latitude
					this.lng = res.longitude
					this.currentAddress = '已获取当前位置'
					this.loadNearby()
				},
				fail: () => {
					uni.showModal({
						title: '定位失败',
						content: '无法获取当前位置，是否手动选择地址？',
						success: (res) => {
							if (res.confirm) this.chooseAddress()
						}
					})
				}
			})
		},
		// 手动选地址（也会返回经纬度）
		chooseAddress() {
			uni.chooseLocation({
				success: (res) => {
					this.lat = res.latitude
					this.lng = res.longitude
					this.currentAddress = res.name || res.address
					this.loadNearby()
				}
			})
		},
		// 拉取附近房源（后端按 Haversine 距离升序返回）
		async loadNearby() {
			try {
				const list = await getNearbyHouses(this.lat, this.lng)
				// 预下载封面图到本地（真机上 image 直接加载 HTTP 图片会被拦截）
				this.houses = await preloadCovers(list)
			} catch (err) {
				console.error('加载附近房源失败', err)
			}
		}
	}
}
</script>

<style scoped>
.page {
	padding: 24rpx 24rpx 140rpx;
}

/* 搜索栏 + 定位 */
.search-row {
	display: flex;
	align-items: center;
}

.search-box {
	flex: 1;
	height: 76rpx;
	display: flex;
	align-items: center;
	padding: 0 24rpx;
	background-color: #FFFFFF;
	border-radius: 999rpx;
}

.search-box__placeholder {
	font-size: 26rpx;
	color: #8A8A8E;
}

.locate-btn {
	margin-left: 16rpx;
	height: 76rpx;
	padding: 0 28rpx;
	display: flex;
	align-items: center;
	background-color: #0FDC78;
	border-radius: 999rpx;
}

.locate-btn__text {
	font-size: 26rpx;
	color: #004D26;
}

.current-loc {
	margin-top: 16rpx;
}

.current-loc__text {
	font-size: 24rpx;
	color: #8A8A8E;
}

.section-title {
	margin: 32rpx 0 16rpx;
}

.section-title__text {
	font-size: 30rpx;
	font-weight: 600;
	color: #1A1A1A;
}

/* 房源卡片 */
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