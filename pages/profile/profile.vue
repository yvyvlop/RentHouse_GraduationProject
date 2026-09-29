<template>
	<view class="page">
		<view class="form">
			<view class="form__item">
				<text class="form__label">姓名</text>
				<input class="form__input" v-model="form.name" placeholder="请输入姓名" />
			</view>

			<view class="form__item">
				<text class="form__label">性别</text>
				<view class="gender">
					<view class="gender__option" :class="{ 'is-active': form.gender === 'male' }"
						@click="form.gender = 'male'">
						<text class="gender__text">男</text>
					</view>
					<view class="gender__option" :class="{ 'is-active': form.gender === 'female' }"
						@click="form.gender = 'female'">
						<text class="gender__text">女</text>
					</view>
				</view>
			</view>

			<view class="form__item">
				<text class="form__label">联系方式</text>
				<input class="form__input" v-model="form.contact" type="number" maxlength="11"
					placeholder="请输入手机号" />
			</view>
		</view>

		<view class="save-btn" @click="save">
			<text class="save-btn__text">保存</text>
		</view>
	</view>
</template>

<script>
import { getUserInfo, updateUserInfo } from '@/utils/request.js'

export default {
	data() {
		return {
			form: {
				name: '',
				gender: 'male',
				contact: ''
			}
		}
	},
	onLoad() {
		this.loadProfile()
	},
	methods: {
		async loadProfile() {
			try {
				const user = await getUserInfo()
				this.form = {
					name: user.name || '',
					// 后端用 1=男 2=女，前端用 male/female
					gender: user.gender === 2 ? 'female' : 'male',
					contact: user.contact || ''
				}
			} catch (err) {
				console.error('加载个人信息失败', err)
			}
		},
		async save() {
			if (!this.form.name.trim()) {
				uni.showToast({
					title: '请输入姓名',
					icon: 'none'
				})
				return
			}
			if (!/^1\d{10}$/.test(this.form.contact)) {
				uni.showToast({
					title: '请输入正确的手机号',
					icon: 'none'
				})
				return
			}

			const gender = this.form.gender === 'male' ? 1 : 2
			try {
				await updateUserInfo({
					name: this.form.name,
					gender,
					contact: this.form.contact
				})

				// 同步本地缓存的用户信息，供其他页面展示
				const user = uni.getStorageSync('user') || {}
				user.name = this.form.name
				user.gender = gender
				user.contact = this.form.contact
				uni.setStorageSync('user', user)

				uni.showToast({ title: '已保存' })
				setTimeout(() => uni.navigateBack(), 600)
			} catch (err) {
				console.error('保存个人信息失败', err)
			}
		}
	}
}
</script>

<style scoped>
	.page {
		padding: 24rpx;
	}

	.form {
		background-color: #FFFFFF;
		border-radius: 16rpx;
		overflow: hidden;
	}

	.form__item {
		display: flex;
		align-items: center;
		min-height: 104rpx;
		padding: 0 32rpx;
		border-bottom: 1rpx solid #F2F2F2;
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

	.save-btn {
		margin-top: 48rpx;
		height: 92rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		background-color: #0FDC78;
		border-radius: 999rpx;
	}

	.save-btn__text {
		font-size: 32rpx;
		color: #004D26;
	}
</style>