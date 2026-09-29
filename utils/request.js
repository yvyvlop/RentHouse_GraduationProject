/**
 * 网络请求封装
 * 统一处理 baseURL、token 携带、错误提示
 */

// 开发环境：后端跑在本机 8080
// 真机调试：改成电脑的局域网 IP（ipconfig | findstr IPv4 查看真实网卡地址）
// 生产环境：改成虚拟机地址，如 http://192.168.105.129:8080
export const BASE_URL = 'http://192.168.1.102:8080'

/**
 * 把后端返回的相对路径拼成完整图片地址
 * 例：/images/2026/09/x.png → http://192.168.1.102:8080/images/2026/09/x.png
 */
export function toFullUrl(path) {
	if (!path) return ''
	if (path.startsWith('http')) return path
	return BASE_URL + path
}

/**
 * 发送请求
 * @param {Object} options - { url, method, data, needAuth }
 * @returns {Promise}
 */
export function request(options) {
	return new Promise((resolve, reject) => {
		// 拼接完整 URL
		const url = BASE_URL + options.url

		// 默认需要 token
		const needAuth = options.needAuth !== false

		// 从本地存储取 token
		const token = uni.getStorageSync('token')
		if (needAuth && !token) {
			// 没有 token，跳登录页
			uni.reLaunch({
				url: '/pages/login/login'
			})
			reject(new Error('未登录'))
			return
		}

		// 请求头
		const header = {
			'Content-Type': 'application/json'
		}
		if (needAuth && token) {
			header['Authorization'] = `Bearer ${token}`
		}

		uni.request({
			url,
			method: options.method || 'GET',
			data: options.data,
			header,
			success: (res) => {
				if (res.statusCode === 200) {
					const data = res.data
					// 后端统一返回 {code, msg, data}
					if (data.code === 200) {
						resolve(data.data)
					} else {
						// 业务错误
						uni.showToast({
							title: data.msg || '请求失败',
							icon: 'none'
						})
						reject(new Error(data.msg || '请求失败'))
					}
				} else {
					// HTTP 错误
					uni.showToast({
						title: `请求失败 ${res.statusCode}`,
						icon: 'none'
					})
					reject(new Error(`HTTP ${res.statusCode}`))
				}
			},
			fail: (err) => {
				uni.showToast({
					title: '网络错误',
					icon: 'none'
				})
				reject(err)
			}
		})
	})
}

/**
 * 登录接口（不需要 token）
 */
export function login(code) {
	return request({
		url: '/api/login',
		method: 'POST',
		data: { code },
		needAuth: false
	})
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
	return request({
		url: '/api/user/me',
		method: 'GET'
	})
}

/**
 * 更新用户信息
 */
export function updateUserInfo(data) {
	return request({
		url: '/api/user/me',
		method: 'PUT',
		data
	})
}

/**
 * 上传单张图片，返回服务器相对路径
 * @param {String} filePath - 本地图片路径（chooseImage 返回的 tempFilePath）
 */
export function uploadImage(filePath) {
	return new Promise((resolve, reject) => {
		const token = uni.getStorageSync('token')
		uni.uploadFile({
			url: BASE_URL + '/api/upload',
			filePath,
			name: 'file',
			header: {
				Authorization: `Bearer ${token}`
			},
			success: (res) => {
				try {
					const data = JSON.parse(res.data)
					if (data.code === 200) {
						resolve(data.data)
					} else {
						reject(new Error(data.msg || '上传失败'))
					}
				} catch (e) {
					reject(new Error('上传返回格式错误'))
				}
			},
			fail: () => reject(new Error('上传失败，请检查网络'))
		})
	})
}

/**
 * 发布房源
 */
export function publishHouse(data) {
	return request({
		url: '/api/house',
		method: 'POST',
		data
	})
}

/**
 * 我发布的房源列表
 */
export function getMyHouses() {
	return request({
		url: '/api/house/mine',
		method: 'GET'
	})
}

/**
 * 预加载图片到本地临时文件
 * 真机上 image 组件直接加载 HTTP 明文图片会被拦截（模拟器正常），
 * 先用 downloadFile 下载到本地临时路径再给 image 使用即可正常显示；
 * 下载失败时回退返回原地址，不影响模拟器使用
 */
export function preloadImage(url) {
	return new Promise((resolve) => {
		if (!url) {
			resolve('')
			return
		}
		uni.downloadFile({
			url,
			success: (res) => {
				resolve(res.statusCode === 200 ? res.tempFilePath : url)
			},
			fail: () => resolve(url)
		})
	})
}

/**
 * 批量预加载房源列表的封面图
 * 入参为后端原始列表（cover 是 /images/... 相对路径），返回可直接绑定到 image 的列表
 */
export async function preloadCovers(list) {
	return Promise.all(list.map(async item => ({
		...item,
		cover: await preloadImage(toFullUrl(item.cover))
	})))
}

/**
 * 房源详情（游客可访问）
 */
export function getHouseDetail(id) {
	return request({
		url: `/api/house/detail/${id}`,
		method: 'GET',
		needAuth: false
	})
}

/**
 * 附近房源（游客可访问），按距离升序
 * @param {Number} lat 纬度
 * @param {Number} lng 经度
 * @param {Number} radius 搜索半径（公里）
 */
export function getNearbyHouses(lat, lng, radius = 5) {
	return request({
		url: `/api/house/nearby?lat=${lat}&lng=${lng}&radius=${radius}`,
		method: 'GET',
		needAuth: false
	})
}

/**
 * 关键词搜索（游客可访问），传经纬度则按距离升序
 */
export function searchHouses(keyword, lat, lng) {
	let url = `/api/house/search?keyword=${encodeURIComponent(keyword || '')}`
	if (lat && lng) {
		url += `&lat=${lat}&lng=${lng}`
	}
	return request({
		url,
		method: 'GET',
		needAuth: false
	})
}

/**
 * 编辑房源
 */
export function updateHouse(id, data) {
	return request({
		url: `/api/house/${id}`,
		method: 'PUT',
		data
	})
}

/**
 * 下架房源
 */
export function deleteHouse(id) {
	return request({
		url: `/api/house/${id}`,
		method: 'DELETE'
	})
}