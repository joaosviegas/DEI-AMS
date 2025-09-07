<template>
  <v-container>
    <v-row class="mt-4">
      <v-col cols="12" class="text-center">
        <h1 class="text-h3 mb-6">Bem-vindo ao <strong>AMS</strong>!</h1>
        <h2 class="text-h5 mb-6">Sistema de Gestão Académica</h2>
      </v-col>
    </v-row>

    <v-row justify="center" class="mb-6">
      <v-col cols="12" md="8">
        <v-alert
          :color="roleColor"
          variant="tonal"
          border="start"
          :title="roleName"
          :icon="roleIcon"
          class="mb-4 text-left"
        >
          {{ roleDescription }}
        </v-alert>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="12">
        <h2 class="text-h5 mb-4 text-center">Funcionalidades Disponíveis:</h2>
      </v-col>
    </v-row>

    <v-row>
      <v-col v-for="(feature, i) in features" :key="i" cols="12" sm="6" md="3">
        <v-card 
          height="100%" 
          class="feature-card"
          @click="handleFeatureClick(feature)"
          style="cursor: pointer"
        >
          <v-card-title class="d-flex align-center">
            <v-icon :icon="feature.icon" class="mr-2" :color="(feature as any).color || roleColor"></v-icon>
            {{ feature.title }}
          </v-card-title>
          <v-card-text>
            {{ feature.description }}
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useRoleStore } from '../stores/role';

const router = useRouter();
const roleStore = useRoleStore();
const currentRole = computed(() => roleStore.currentActiveRole);

const roleName = computed(() => {
  switch(currentRole.value) {
    case 'ADMINISTRATOR': return 'Administrador';
    case 'MAIN_TEACHER': return 'Professor Regente';
    case 'TEACHING_ASSISTANT': return 'Professor Assistente';
    case 'STUDENT': return 'Aluno';
    default: return 'Selecionar Papel';
  }
});

const roleColor = computed(() => {
  switch(currentRole.value) {
    case 'ADMINISTRATOR': return 'purple';
    case 'MAIN_TEACHER': return 'red';
    case 'TEACHING_ASSISTANT': return 'blue';
    case 'STUDENT': return 'green';
    default: return 'grey';
  }
});

const roleIcon = computed(() => {
  switch(currentRole.value) {
    case 'ADMINISTRATOR': return 'mdi-shield-account';
    case 'MAIN_TEACHER': return 'mdi-account-tie';
    case 'TEACHING_ASSISTANT': return 'mdi-account-school';
    case 'STUDENT': return 'mdi-school';
    default: return 'mdi-account-question';
  }
});

const roleDescription = computed(() => {
  switch(currentRole.value) {
    case 'ADMINISTRATOR':
      return 'Como Administrador, você tem acesso completo ao sistema para gerir pessoas, cursos, unidades curriculares e visualizar estatísticas.';
    case 'MAIN_TEACHER':
      return 'Como Professor Regente, você pode gerir as suas unidades curriculares, adicionar Professores Assistentes e Alunos.';
    case 'TEACHING_ASSISTANT':
      return 'Como Professor Assistente, você pode visualizar as unidades curriculares onde está envolvido e consultar informações dos Alunos.';
    case 'STUDENT':
      return 'Como Aluno, você pode visualizar as suas unidades curriculares, avaliações, professores e colegas.';
    default:
      return 'Selecione um papel abaixo para acessar as funcionalidades do sistema.';
  }
});

const features = computed(() => {
  // If no role is selected, show role selection options
  if (!currentRole.value || currentRole.value === '') {
    return [
      {
        title: 'Aluno',
        description: 'Visualizar as suas unidades curriculares, professores e colegas de turma.',
        icon: 'mdi-school',
        color: 'green',
        role: 'STUDENT',
        isRoleSelection: true
      },
      {
        title: 'Professor Assistente',
        description: 'Visualizar as unidades curriculares onde está envolvido e consultar informações dos alunos.',
        icon: 'mdi-account-school',
        color: 'blue',
        role: 'TEACHING_ASSISTANT',
        isRoleSelection: true
      },
      {
        title: 'Professor Regente',
        description: 'Gerir as suas unidades curriculares, adicionar professores assistentes e alunos.',
        icon: 'mdi-account-tie',
        color: 'red',
        role: 'MAIN_TEACHER',
        isRoleSelection: true
      },
      {
        title: 'Administrador',
        description: 'Acesso completo ao sistema para gerir pessoas, cursos, unidades curriculares e estatísticas.',
        icon: 'mdi-shield-account',
        color: 'purple',
        role: 'ADMINISTRATOR',
        isRoleSelection: true
      }
    ];
  }

  switch(currentRole.value) {
    case 'ADMINISTRATOR':
      return [
        {
          title: 'Gestão de Pessoas',
          description: 'Adicione, edite e gerencie utilizadores do sistema.',
          icon: 'mdi-account-group',
          route: '/people'
        },
        {
          title: 'Gestão de Cursos',
          description: 'Crie e gerencie os cursos disponíveis no departamento.',
          icon: 'mdi-book-multiple',
          route: '/courses'
        },
        {
          title: 'Unidades Curriculares',
          description: 'Gerencie todas as unidades curriculares, professores e Alunos.',
          icon: 'mdi-school',
          route: '/curricular-units'
        },
        {
          title: 'Estatísticas',
          description: 'Consulte estatísticas sobre cursos, UCs e utilizadores.',
          icon: 'mdi-chart-bar',
          route: '/statistics'
        }
      ];
    case 'MAIN_TEACHER':
      return [
        {
          title: 'Minhas UCs',
          description: 'Consulte as UCs onde você é Professor Regente.',
          icon: 'mdi-school',
          route: '/curricular-units'
        },
        {   
          title: 'Workflow de Revisões',
          description: 'Visualize as tarefas de correção que lhe faltam aprovar.',
          icon: 'mdi-clipboard-edit',
          //route: TODO
        },
        {
          title: 'Cursos',
          description: 'Consulte informações sobre os cursos e gestão das suas pessoas.',
          icon: 'mdi-book-multiple',
          route: '/courses'
        },
        {
          title: 'Estatísticas',
          description: 'Visualize estatísticas das suas unidades curriculares.',
          icon: 'mdi-chart-bar',
          route: '/statistics'
        }
      ];
    case 'TEACHING_ASSISTANT':
      return [
        {
          title: 'Unidades Curriculares',
          description: 'Visualize as UCs onde você é professor assistente.',
          icon: 'mdi-school',
          route: '/curricular-units'
        },
        {   
          title: 'Workflow de Revisões',
          description: 'Visualize as tarefas de correção que lhe foram atribuídas.',
          icon: 'mdi-clipboard-edit',
          //route: TODO
        },
        {
          title: 'Alunos',
          description: 'Consulte informações sobre os Alunos das suas UCs.',
          icon: 'mdi-account-group',
          route: '/people'
        },
        {
          title: 'Cursos',
          description: 'Consulte informações sobre os cursos relacionados.',
          icon: 'mdi-book-multiple',
          route: '/courses'
        }
      ];
    case 'STUDENT':
      return [
        {
          title: 'Minhas UCs',
          description: 'Visualize as UCs onde você está inscrito.',
          icon: 'mdi-school',
          route: '/curricular-units'
        },
        {
          title: 'Prazos & Entregas',
          description: 'Consulte as avaliações e as datas das mesmas.',
          icon: 'mdi-clock-alert-outline',
          route: '/deadlines'
        },
        {
          title: 'Cursos',
          description: 'Visualize informações sobre cursos.',
          icon: 'mdi-book-multiple',
          route: '/courses'
        },
        {
          title: 'Estatísticas',
          description: 'Visualize estatísticas das suas unidades curriculares.',
          icon: 'mdi-chart-bar',
          route: '/statistics'
        }
      ];
    default:
      return [];
  }
});

const handleFeatureClick = (feature: any) => {
  if (feature.isRoleSelection) {
    // Switch to the selected perspective/role
    roleStore.switchPerspective(feature.role);
  } else if (feature.route) {
    // Navigate to the route
    router.push(feature.route);
  }
};
</script>

<style scoped>
.feature-card {
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

.feature-card:active {
  transform: translateY(-2px);
}
</style>
