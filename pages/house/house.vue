<template>
	<view class="page">
		<swiper class="gallery" :indicator-dots="true" indicator-active-color="#FFFFFF" circular>
			<swiper-item v-for="(img, index) in house.images" :key="index">
				<image class="gallery__img" :src="img" mode="aspectFill" @click="preview(index)"></image>
			</swiper-item>
		</swiper>

		<view class="card">
			<text class="house-title">{{ house.title }}</text>
			<text class="house-price">{{ house.price }} 元/月</text>

			<view class="info-row">
				<text class="info-row__label">房间大小</text>
				<text class="info-row__value">{{ house.area }} ㎡</text>
			</view>
			<view class="info-row">
				<text class="info-row__label">房间格局</text>
				<text class="info-row__value">{{ house.layout }}</text>
			</view>
			<view class="info-row">
				<text class="info-row__label">详细地址</text>
				<text class="info-row__value">{{ house.address }}</text>
			</view>
		</view>

		<view class="card">
			<text class="card__title">房东信息</text>
			<view class="info-row">
				<text class="info-row__label">姓名</text>
				<text class="info-row__value">{{ house.landlord.name }}</text>
			</view>
			<view class="info-row">
				<text class="info-row__label">性别</text>
				<text class="info-row__value">{{ house.landlord.gender === 2 ? '女' : '男' }}</text>
			</view>
			<view class="info-row">
				<text class="info-row__label">联系方式</text>
				<text class="info-row__value is-link" @click="callLandlord">{{ house.landlord.contact }}</text>
			</view>
		</view>
	</view>
</template>

<script>
import { getHouseDetail, toFullUrl, preloadImage } from '@/utils/request.js'

export default {
	data() {
		return {
			house: {
				id: 0,
				title: '',
				price: 0,
				area: 0,
				layout: '',
				address: '',
				images: [],
				landlord: {
					name: '',
					gender: 1,
					contact: ''
				}
			}
		}
	},
	onLoad(options) {
		this.loadDetail(options.id)
	},
	methods: {
		async loadDetail(id) {
			try {
				const data = await getHouseDetail(id)
				// 预下载图片到本地（真机上 image 直接加载 HTTP 图片会被拦截）
				const images = await Promise.all(
					(data.images || []).map(url => preloadImage(toFullUrl(url)))
				)
				this.house = {
					id: data.id,
					title: data.title,
					price: data.price,
					area: data.area,
					layout: data.layout,
					address: data.address,
					images,
					landlord: {
						name: data.landlordName || '未填写',
						gender: data.landlordGender,
						contact: data.landlordContact || '未填写'
					}
				}
			} catch (err) {
				console.error('加载房源详情失败', err)
			}
		},
		preview(index) {
			uni.previewImage({
				urls: this.house.images,
				current: index
			})
		},
		callLandlord() {
			if (!this.house.landlord.contact || this.house.landlord.contact === '未填写') {
				uni.showToast({ title: '房东未填写联系方式', icon: 'none' })
				return
			}
			uni.makePhoneCall({
				phoneNumber: this.house.landlord.contact
			})
		}
	}
}
</script>

<style scoped>
.page {
	padding-bottom: 40rpx;
}

.gallery {
	width: 100%;
	height: 480rpx;
}

.gallery__img {
	width: 100%;
	height: 100%;
	background-color: #F2F2F2;
}

.card {
	margin: 24rpx;
	padding: 32rpx;
	background-color: #FFFFFF;
	border-radius: 16rpx;
}

.card__title {
	display: block;
	margin-bottom: 16rpx;
	font-size: 30rpx;
	font-weight: 600;
	color: #1A1A1A;
}

.house-title {
	display: block;
	font-size: 36rpx;
	font-weight: 600;
	color: #1A1A1A;
}

.house-price {
	display: block;
	margin-top: 12rpx;
	font-size: 40rpx;
	font-weight: 600;
	color: #FF5B3B;
}

.info-row {
	display: flex;
	align-items: flex-start;
	margin-top: 20rpx;
}

.info-row__label {
	width: 140rpx;
	font-size: 26rpx;
	color: #8A8A8E;
	flex-shrink: 0;
}

.info-row__value {
	flex: 1;
	font-size: 26rpx;
	color: #1A1A1A;
}

.info-row__value.is-link {
	color: #0FDC78;
}
</style>