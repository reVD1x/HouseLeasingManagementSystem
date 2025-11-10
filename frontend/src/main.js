import './assets/main.css'
import './assets/modules.css'

import { createApp } from 'vue'
import App from './App.vue'
import { createRouter, createWebHashHistory } from 'vue-router'
import Houses from './components/Houses.vue'
import AddHouse from './components/AddHouse.vue'
import EditHouse from './components/EditHouse.vue'

import Tenants from './components/Tenants.vue'
import AddTenant from './components/AddTenant.vue'
import EditTenant from './components/EditTenant.vue'

import Login from './components/Login.vue'

import Contracts from './components/Contracts.vue'
import AddContract from './components/AddContract.vue'
import EditContract from './components/EditContract.vue'

import RentPayments from './components/RentPayments.vue'
import AddRentPayment from './components/AddRentPayment.vue'
import EditRentPayment from './components/EditRentPayment.vue'

const routes = [
	{ path: '/', redirect: '/houses' },
	{ path: '/login', component: Login },
	{ path: '/houses', component: Houses },
	{ path: '/houses/add', component: AddHouse },
	{ path: '/houses/edit/:id', component: EditHouse, props: true },

	{ path: '/tenants', component: Tenants },
	{ path: '/tenants/add', component: AddTenant },
	{ path: '/tenants/edit/:id', component: EditTenant, props: true },

	{ path: '/contracts', component: Contracts },
	{ path: '/contracts/add', component: AddContract },
	{ path: '/contracts/edit/:id', component: EditContract, props: true },

	{ path: '/rent-payments', component: RentPayments },
	{ path: '/rent-payments/add', component: AddRentPayment },
	{ path: '/rent-payments/edit/:id', component: EditRentPayment, props: true }
]

const router = createRouter({ history: createWebHashHistory(), routes })

const app = createApp(App)
app.use(router)
app.mount('#app')
