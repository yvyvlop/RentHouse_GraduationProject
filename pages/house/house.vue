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
				<text class="info-row__value">{{ house.landlord.gender === 'female' ? '女' : '男' }}</text>
			</view>
			<view class="info-row">
				<text class="info-row__label">联系方式</text>
				<text class="info-row__value is-link" @click="callLandlord">{{ house.landlord.contact }}</text>
			</view>
		</view>
	</view>
</template>

<script>
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
						gender: 'male',
						contact: ''
					}
				}
			}
		},
		onLoad(options) {
			const id = options.id
			// TODO 接后端接口：用 id 拉取房源详情和房东信息
			this.house = {
				id: id,
				title: '示例房源 · 阳光单间',
				price: 1500,
				area: 25,
				layout: '1室1厅1卫',
				address: '示例市示例区示例路 100 号',
				images: ['/static/logo.png', '/static/logo.png'],
				landlord: {
					name: '张先生',
					gender: 'male',
					contact: '13800000000'
				}
			}
		},
		methods: {
			preview(index) {
				uni.previewImage({
					urls: this.house.images,
					current: index
				})
			},
			callLandlord() {
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