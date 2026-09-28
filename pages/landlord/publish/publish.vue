<template>
	<view class="page">
		<view class="form">
			<!-- 详细地址 -->
			<view class="form__item" @click="chooseAddress">
				<text class="form__label">详细地址</text>
				<text class="form__value" :class="{ 'is-placeholder': !form.address }">
					{{ form.address || '点击选择地址' }}
				</text>
			</view>

			<!-- 房间大小 -->
			<view class="form__item">
				<text class="form__label">房间大小</text>
				<input class="form__input" v-model="form.area" type="digit" placeholder="如 25" />
				<text class="form__unit">㎡</text>
			</view>

			<!-- 格局 -->
			<view class="form__item">
				<text class="form__label">格局</text>
				<input class="form__input" v-model="form.layout" placeholder="如 1室1厅1卫" />
			</view>

			<!-- 租金 -->
			<view class="form__item">
				<text class="form__label">租金</text>
				<input class="form__input" v-model="form.price" type="number" placeholder="如 1500" />
				<text class="form__unit">元/月</text>
			</view>
		</view>

		<!-- 房间真实图片 -->
		<view class="block">
			<text class="block__title">房间真实图片</text>
			<view class="image-list">
				<view class="image-item" v-for="(img, index) in form.images" :key="index">
					<image class="image-item__img" :src="img" mode="aspectFill"></image>
					<view class="image-item__del" @click.stop="removeImage(index)">
						<text class="image-item__del-text">×</text>
					</view>
				</view>
				<view class="image-add" v-if="form.images.length < 9" @click="chooseImages">
					<text class="image-add__text">+</text>
				</view>
			</view>
			<text class="block__tip">最多 9 张，请上传房间真实照片</text>
		</view>

		<!-- 房东信息 -->
		<view class="block">
			<text class="block__title">房东信息</text>
			<view class="form__item form__item--inline">
				<text class="form__label">姓名</text>
				<input class="form__input" v-model="landlord.name" placeholder="请输入姓名" />
			</view>
			<view class="form__item form__item--inline">
				<text class="form__label">性别</text>
				<view class="gender">
					<view class="gender__option" :class="{ 'is-active': landlord.gender === 'male' }"
						@click="landlord.gender = 'male'">
						<text class="gender__text">男</text>
					</view>
					<view class="gender__option" :class="{ 'is-active': landlord.gender === 'female' }"
						@click="landlord.gender = 'female'">
						<text class="gender__text">女</text>
					</view>
				</view>
			</view>
			<view class="form__item form__item--inline">
				<text class="form__label">联系方式</text>
				<input class="form__input" v-model="landlord.contact" type="number" maxlength="11"
					placeholder="请输入手机号" />
			</view>
		</view>

		<view class="submit-btn" @click="submit">
			<text class="submit-btn__text">提交发布</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				form: {
					address: '',
					lat: 0,
					lng: 0,
					area: '',
					layout: '',
					price: '',
					images: []
				},
				landlord: {
					name: '',
					gender: 'male',
					contact: ''
				}
			}
		},
		onLoad() {
			// 房东信息默认取「我的」里保存过的资料，避免每次重填
			this.landlord = {
				name: uni.getStorageSync('name') || '',
				gender: uni.getStorageSync('gender') || 'male',
				contact: uni.getStorageSync('contact') || ''
			}
		},
		methods: {
			chooseAddress() {
				uni.chooseLocation({
					success: (res) => {
						this.form.address = res.address || res.name
						this.form.lat = res.latitude
						this.form.lng = res.longitude
					}
				})
			},
			chooseImages() {
				uni.chooseImage({
					count: 9 - this.form.images.length,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						// TODO 先压缩再上传到服务器，拿到 url 后替换本地路径
						this.form.images = this.form.images.concat(res.tempFilePaths)
					}
				})
			},
			removeImage(index) {
				this.form.images.splice(index, 1)
			},
			submit() {
				if (!this.form.address) return this.tip('请选择详细地址')
				if (!this.form.area) return this.tip('请填写房间大小')
				if (!this.form.layout) return this.tip('请填写房间格局')
				if (!this.form.price) return this.tip('请填写租金')
				if (!this.form.images.length) return this.tip('请上传至少 1 张房间图片')
				if (!this.landlord.name.trim()) return this.tip('请填写房东姓名')
				if (!/^1\d{10}$/.test(this.landlord.contact)) return this.tip('请填写正确的联系方式')

				// TODO 提交到后端；同时把房东信息存本地，下次自动带入
				uni.setStorageSync('name', this.landlord.name)
				uni.setStorageSync('gender', this.landlord.gender)
				uni.setStorageSync('contact', this.landlord.contact)

				uni.showToast({
					title: '发布成功'
				})
				setTimeout(() => uni.navigateBack(), 600)
			},
			tip(title) {
				uni.showToast({
					title,
					icon: 'none'
				})
			}
		}
	}
</script>

<style scoped>
	.page {
		padding: 24rpx;
	}

	.form,
	.block {
		margin-bottom: 24rpx;
		background-color: #FFFFFF;
		border-radius: 16rpx;
		overflow: hidden;
	}

	.block {
		padding: 32rpx;
	}

	.block__title {
		display: block;
		font-size: 30rpx;
		font-weight: 600;
		color: #1A1A1A;
	}

	.block__tip {
		display: block;
		margin-top: 16rpx;
		font-size: 22rpx;
		color: #8A8A8E;
	}

	.form__item {
		display: flex;
		align-items: center;
		min-height: 104rpx;
		padding: 0 32rpx;
		border-bottom: 1rpx solid #F2F2F2;
	}

	.form__item--inline {
		padding: 0;
	}

	.form__item:last-child {
		border-bottom: none;
	}

	.form__label {
		width: 160rpx;
		font-size: 28rpx;
		color: #8A8A8E;
		flex-shrink: 0;
	}

	.form__input {
		flex: 1;
		font-size: 28rpx;
		color: #1A1A1A;
	}

	.form__value {
		flex: 1;
		font-size: 28rpx;
		color: #1A1A1A;
	}

	.form__value.is-placeholder {
		color: #C8C8CC;
	}

	.form__unit {
		margin-left: 12rpx;
		font-size: 26rpx;
		color: #8A8A8E;
	}

	/* 图片 */
	.image-list {
		display: flex;
		flex-wrap: wrap;
		margin-top: 24rpx;
	}

	.image-item {
		position: relative;
		width: 200rpx;
		height: 200rpx;
		margin: 0 16rpx 16rpx 0;
	}

	.image-item__img {
		width: 100%;
		height: 100%;
		border-radius: 12rpx;
		background-color: #F2F2F2;
	}

	.image-item__del {
		position: absolute;
		top: -12rpx;
		right: -12rpx;
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		background-color: rgba(0, 0, 0, 0.6);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.image-item__del-text {
		font-size: 28rpx;
		color: #FFFFFF;
		line-height: 1;
	}

	.image-add {
		width: 200rpx;
		height: 200rpx;
		border-radius: 12rpx;
		background-color: #F5F5F7;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.image-add__text {
		font-size: 60rpx;
		color: #C8C8CC;
		line-height: 1;
	}

	/* 性别 */
	.gender {
		display: flex;
	}

	.gender__option {
		height: 60rpx;
		padding: 0 32rpx;
		margin-right: 16rpx;
		display: flex;
		align-items: center;
		background-color: #F5F5F7;
		border-radius: 999rpx;
	}

	.gender__option.is-active {
		background-color: #E5FFF2;
	}

	.gender__text {
		font-size: 26rpx;
		color: #1A1A1A;
	}

	.submit-btn {
		margin-top: 48rpx;
		height: 92rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #0FDC78;
		border-radius: 999rpx;
	}

	.submit-btn__text {
		font-size: 32rpx;
		color: #004D26;
	}
</style>