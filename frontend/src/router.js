import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/houses' },
  { path: '/houses', component: () => import('./components/Houses.vue') },
  { path: '/houses/add', component: () => import('./components/AddHouse.vue') },
  { path: '/houses/edit/:id', component: () => import('./components/EditHouse.vue') },

  { path: '/tenants', component: () => import('./components/Tenants.vue') },
  { path: '/tenants/add', component: () => import('./components/AddTenant.vue') },
  { path: '/tenants/edit/:id', component: () => import('./components/EditTenant.vue') },

  { path: '/contracts', component: () => import('./components/Contracts.vue') },
  { path: '/contracts/add', component: () => import('./components/AddContract.vue') },
  { path: '/contracts/edit/:id', component: () => import('./components/EditContract.vue') },

  { path: '/rent-payments', component: () => import('./components/RentPayments.vue') },
  { path: '/rent-payments/add', component: () => import('./components/AddRentPayment.vue') },
  { path: '/rent-payments/edit/:id', component: () => import('./components/EditRentPayment.vue') },

  { path: '/login', component: () => import('./components/Login.vue') },
]

const router = createRouter({ history: createWebHashHistory(), routes })

export default router
