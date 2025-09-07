import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import PeopleView from '@/views/people/PeopleView.vue'
import StatisticsView from '@/views/statistics/StatisticsView.vue'
import CoursesView from '@/views/courses/CoursesView.vue'
import CurricularUnitsView from '@/views/curricularUnits/CurricularUnitsView.vue'
import DeadlinesView from '@/views/deadlines/DeadlinesView.vue'
import StudentsView from '@/views/students/StudentsView.vue'
import WorkflowView from '@/views/workflow/WorkflowView.vue'
import CalendarView from '@/views/calendar/CalendarView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/curricular-units',
      name: 'curricular-units',
      component: CurricularUnitsView
    },
    {
      path: '/courses',
      name: 'courses',
      component: CoursesView
    },
    {
      path: '/people',
      name: 'people',
      component: PeopleView
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: StatisticsView
    },
    {
      path: '/deadlines',
      name: 'deadlines',
      component: DeadlinesView
    },
    {
      path: '/students',
      name: 'students',
      component: StudentsView
    },
    {
      path: '/workflow',
      name: 'workflow',
      component: WorkflowView
    },
    {
      path: '/calendar',
      name: 'calendar',
      component: CalendarView
    }
  ]
})

export default router
