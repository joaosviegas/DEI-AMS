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
        <h2 class="text-h5 mb-4 text-center">Funcionalidades Disponíveis</h2>
      </v-col>
    </v-row>

    <v-row>
      <v-col v-for="(feature, i) in features" :key="i" cols="12" sm="6" md="4">
        <v-card height="100%">
          <v-card-title class="d-flex align-center">
            <v-icon :icon="feature.icon" class="mr-2" :color="roleColor"></v-icon>
            {{ feature.title }}
          </v-card-title>
          <v-card-text>
            {{ feature.description }}
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn
              :to="feature.route"
              :color="roleColor"
              variant="tonal"
              size="small"
            >
              Acessar
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoleStore } from '../stores/role';

const roleStore = useRoleStore();
const currentRole = computed(() => roleStore.currentActiveRole);

const roleName = computed(() => {
  switch(currentRole.value) {
    case 'ADMINISTRATOR': return 'Administrador';
    case 'MAIN_TEACHER': return 'Professor Regente';
    case 'TEACHING_ASSISTANT': return 'Professor Assistente';
    case 'STUDENT': return 'Aluno';
    default: return 'Usuário';
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
    default: return 'mdi-account';
  }
});

const roleDescription = computed(() => {
  switch(currentRole.value) {
    case 'ADMINISTRATOR':
      return 'Como Administrador, você tem acesso completo ao sistema para gerir pessoas, cursos, unidades curriculares e visualizar estatísticas.';
    case 'MAIN_TEACHER':
      return 'Como Professor Regente, você pode gerir as suas unidades curriculares, adicionar professores assistentes e Alunos.';
    case 'TEACHING_ASSISTANT':
      return 'Como Professor Assistente, você pode visualizar as unidades curriculares onde está envolvido e consultar informações dos Alunos.';
    case 'STUDENT':
      return 'Como Aluno, você pode visualizar as suas unidades curriculares, professores e colegas de turma.';
    default:
      return 'Selecione um role para visualizar as funcionalidades.';
  }
});

const features = computed(() => {
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
          title: 'Minhas Unidades Curriculares',
          description: 'Gerencie as UCs onde você é professor regente.',
          icon: 'mdi-school',
          route: '/curricular-units'
        },
        {
          title: 'Gestão de Pessoas',
          description: 'Adicione professores assistentes e Alunos às suas UCs.',
          icon: 'mdi-account-plus',
          route: '/people'
        },
        {
          title: 'Cursos',
          description: 'Consulte informações sobre os cursos relacionados.',
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
          title: 'Minhas Unidades Curriculares',
          description: 'Visualize as UCs onde você está inscrito.',
          icon: 'mdi-school',
          route: '/curricular-units'
        },
        {
          title: 'Professores e Colegas',
          description: 'Consulte informações sobre professores e colegas.',
          icon: 'mdi-account-group',
          route: '/people'
        },
        {
          title: 'Meu Curso',
          description: 'Visualize informações sobre o seu curso.',
          icon: 'mdi-book-multiple',
          route: '/courses'
        }
      ];
    default:
      return [];
  }
});
</script>
