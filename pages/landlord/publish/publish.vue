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
				<view class="image-item" v-for="(img, index) in displayImages" :key="index">
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
			<text class="submit-btn__text">{{ houseId ? '保存修改' : '提交发布' }}</text>
		</view>
	</view>
</template>

<script>
import {
	uploadImage,
	publishHouse,
	updateHouse,
	getHouseDetail,
	updateUserInfo,
	toFullUrl
} from '@/utils/request.js'

export default {
	data() {
		return {
			houseId: null, // 有值表示编辑模式
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
	onLoad(options) {
		// 编辑模式：拉取房源详情回填
		if (options && options.id) {
			this.houseId = options.id
			uni.setNavigationBarTitle({ title: '编辑房源' })
			this.loadDetail()
		}
		// 房东信息默认取本地缓存的用户资料，避免每次重填
		const user = uni.getStorageSync('user')
		if (user) {
			this.landlord = {
				name: user.name || '',
				gender: user.gender === 2 ? 'female' : 'male',
				contact: user.contact || ''
			}
		}
	},
	computed: {
		// 图片显示地址：服务器相对路径拼 baseURL，本地临时路径直接用
		// 用计算属性预处理好，避免在小程序模板中调用方法（真机上可能不生效）
		displayImages() {
			return this.form.images.map(path =>
				path.startsWith('/images') ? toFullUrl(path) : path
			)
		}
	},
	methods: {
		async loadDetail() {
			try {
				const house = await getHouseDetail(this.houseId)
				this.form = {
					address: house.address,
					lat: house.lat,
					lng: house.lng,
					area: house.area,
					layout: house.layout,
					price: house.price,
					images: house.images || []
				}
				if (house.landlordName) {
					this.landlord = {
						name: house.landlordName || '',
						gender: house.landlordGender === 2 ? 'female' : 'male',
						contact: house.landlordContact || ''
					}
				}
			} catch (err) {
				console.error('加载房源详情失败', err)
			}
		},
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
					// 先存本地路径，提交时统一上传
					this.form.images = this.form.images.concat(res.tempFilePaths)
				}
			})
		},
		removeImage(index) {
			this.form.images.splice(index, 1)
		},
		async submit() {
			// 表单校验
			if (!this.form.address) return this.tip('请选择详细地址')
			if (!this.form.area) return this.tip('请填写房间大小')
			if (!this.form.layout) return this.tip('请填写房间格局')
			if (!this.form.price) return this.tip('请填写租金')
			if (!this.form.images.length) return this.tip('请上传至少 1 张房间图片')
			if (!this.landlord.name.trim()) return this.tip('请填写房东姓名')
			if (!/^1\d{10}$/.test(this.landlord.contact)) return this.tip('请填写正确的联系方式')

			uni.showLoading({ title: '提交中', mask: true })
			try {
				// 1. 上传未上传过的图片（本地临时路径需要上传，服务器路径直接复用）
				const serverImages = []
				for (const img of this.form.images) {
					if (img.startsWith('/images')) {
						serverImages.push(img)
					} else {
						const url = await uploadImage(img)
						serverImages.push(url)
					}
				}

				// 2. 同步房东信息到用户资料
				const gender = this.landlord.gender === 'male' ? 1 : 2
				await updateUserInfo({
					name: this.landlord.name,
					gender,
					contact: this.landlord.contact
				})
				// 同步本地缓存，下次自动带入
				const user = uni.getStorageSync('user') || {}
				user.name = this.landlord.name
				user.gender = gender
				user.contact = this.landlord.contact
				uni.setStorageSync('user', user)

				// 3. 提交房源
				const houseData = {
					address: this.form.address,
					lat: this.form.lat,
					lng: this.form.lng,
					area: Number(this.form.area),
					layout: this.form.layout,
					price: Number(this.form.price),
					imageList: serverImages
				}
				if (this.houseId) {
					await updateHouse(this.houseId, houseData)
				} else {
					await publishHouse(houseData)
				}

				uni.hideLoading()
				uni.showToast({ title: this.houseId ? '保存成功' : '发布成功' })
				setTimeout(() => uni.navigateBack(), 800)
			} catch (err) {
				uni.hideLoading()
				console.error('提交失败', err)
			}
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