<template>
	<view class="detail-btn" :class="size" @click.stop="goDetail">
		<text class="detail-btn__text">{{ text }}</text>
	</view>
</template>

<script>
export default {
	name: 'DetailBtn',
	props: {
		// 房源 id，用于拼接详情页参数
		houseId: {
			type: [String, Number],
			required: true
		},
		text: {
			type: String,
			default: '查看详情'
		},
		// default | small
		size: {
			type: String,
			default: 'default'
		}
	},
	methods: {
		goDetail() {
			uni.navigateTo({
				url: `/pages/house/house?id=${this.houseId}`
			})
		}
	}
}
</script>

<style scoped>
.detail-btn {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	height: 64rpx;
	padding: 0 32rpx;
	border-radius: 999rpx;
	background-color: #0FDC78;
}

.detail-btn__text {
	font-size: 26rpx;
	color: #004D26;
}

.detail-btn.small {
	height: 52rpx;
	padding: 0 24rpx;
}

.detail-btn.small .detail-btn__text {
	font-size: 24rpx;
}
</style>