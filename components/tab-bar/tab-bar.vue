<template>
	<view class="tab-bar">
		<view class="tab-bar__item" v-for="item in list" :key="item.key" @click="go(item)">
			<text class="tab-bar__label" :class="{ 'is-active': current === item.key }">{{ item.text }}</text>
		</view>
	</view>
</template>

<script>
export default {
	name: 'TabBar',
	props: {
		// home | mine
		current: {
			type: String,
			required: true
		}
	},
	computed: {
		list() {
			// 切换身份时用的是 reLaunch，页面会重建，这里能读到最新身份
			const role = uni.getStorageSync('role') || 'tenant'
			return [{
				key: 'home',
				text: '主页',
				path: role === 'landlord' ? '/pages/landlord/home/home' : '/pages/home/home'
			},
			{
				key: 'mine',
				text: '我的',
				path: '/pages/mine/mine'
			}
			]
		}
	},
	methods: {
		go(item) {
			if (item.key === this.current) return
			uni.reLaunch({
				url: item.path
			})
		}
	}
}
</script>

<style scoped>
.tab-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	display: flex;
	height: 100rpx;
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
	background-color: #FFFFFF;
	border-top: 1rpx solid #EEEEEE;
	z-index: 999;
}

.tab-bar__item {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

.tab-bar__label {
	font-size: 28rpx;
	color: #8A8A8E;
}

.tab-bar__label.is-active {
	color: #0FDC78;
	font-weight: 600;
}
</style>