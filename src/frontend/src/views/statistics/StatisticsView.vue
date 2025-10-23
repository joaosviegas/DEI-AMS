<template>
  <v-container fluid class="pa-4">
    <!-- Student View -->
    <div v-if="roleStore.isStudent">
      <!-- Header with role color accent -->
      <v-row class="mb-3">
        <v-col cols="12">
          <v-card class="elevation-3 role-header-card" :style="{ borderLeft: `6px solid ${roleColor}` }">
            <v-card-text class="pa-4">
              <div class="d-flex align-center justify-space-between">
                <div>
                  <h1 class="text-h5 mb-1">{{ studentProfile?.name || 'Student Profile' }}</h1>
                  <p class="text-subtitle-2 text-medium-emphasis mb-0">{{ studentProfile?.istId || 'ist109685' }}</p>
                  <p class="text-caption text-medium-emphasis">{{ studentProfile?.email || '' }}</p>
                </div>
                <v-avatar size="60" :color="roleColor">
                  <v-icon size="35" color="white">mdi-school</v-icon>
                </v-avatar>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Stats Cards -->
      <v-row class="mb-3">
        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/curricular-units">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-school</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ enrolledUCs.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Inscrições em UCs</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/curricular-units">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" color="success" class="mb-2">mdi-check-circle</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ passedUCs.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">UCs Aprovadas</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/deadlines">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" color="info" class="mb-2">mdi-account-group</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ studentGroups.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Grupos de Projeto</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/calendar">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" color="warning" class="mb-2">mdi-clock-alert</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ upcomingEvaluations.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Avaliações Futuras</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Content Cards -->
      <v-row>
        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-book-open-variant</v-icon>
              <span>Cursos e UCs</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-0" style="max-height: 280px; overflow-y: auto;">
              <v-list density="compact" class="py-0">
                <template v-if="enrolledUCs.length > 0">
                  <v-list-item v-for="uc in enrolledUCs" :key="uc.id">
                    <template v-slot:prepend>
                      <v-icon size="20" :color="uc.status === 'APPROVED' ? 'success' : roleColor">
                        {{ uc.status === 'APPROVED' ? 'mdi-check-circle' : 'mdi-book' }}
                      </v-icon>
                    </template>
                    <v-list-item-title class="text-body-2">{{ uc.curricularUnit?.name }}</v-list-item-title>
                    <v-list-item-subtitle class="text-caption">
                      {{ uc.curricularUnit?.code }} - {{ uc.status }}
                      <v-chip v-if="uc.finalGrade" size="x-small" :color="uc.status === 'APPROVED' ? 'success' : 'default'" class="ml-1">
                        {{ uc.finalGrade.toFixed(1) }}
                      </v-chip>
                    </v-list-item-subtitle>
                  </v-list-item>
                </template>
                <v-list-item v-else>
                  <v-list-item-title class="text-center text-medium-emphasis text-caption">Sem Inscrições</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-calendar-clock</v-icon>
              <span>Avaliações Futuras</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-0" style="max-height: 280px; overflow-y: auto;">
              <v-list density="compact" class="py-0">
                <template v-if="upcomingEvaluations.length > 0">
                  <v-list-item v-for="evaluation in upcomingEvaluations" :key="evaluation.id">
                    <template v-slot:prepend>
                      <v-icon size="20" :color="evaluation.type === 'TEST' ? 'warning' : 'info'">
                        {{ evaluation.type === 'TEST' ? 'mdi-file-document' : 'mdi-folder' }}
                      </v-icon>
                    </template>
                    <v-list-item-title class="text-body-2">{{ evaluation.title }}</v-list-item-title>
                    <v-list-item-subtitle class="text-caption">
                      {{ formatDate(evaluation.date) }} 
                      <v-chip size="x-small" class="ml-1">{{ evaluation.weight*100 }}%</v-chip>
                    </v-list-item-subtitle>
                  </v-list-item>
                </template>
                <v-list-item v-else>
                  <v-list-item-title class="text-center text-medium-emphasis text-caption">Sem Avaliações Futuras</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Project Groups -->
      <v-row v-if="studentGroups.length > 0">
        <v-col cols="12">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-account-group</v-icon>
              <span>Grupos de Projeto</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-3">
              <v-row dense>
                <v-col v-for="group in studentGroups" :key="group.id" cols="12" sm="6" md="4">
                  <v-card class="h-100 pa-3" :style="{ border: `2px solid ${roleColor}`, borderRadius: '8px' }">
                    <div class="d-flex flex-column">
                      <div class="d-flex align-center mb-2">
                        <v-chip size="small" :color="roleColor" class="mr-2">
                          Grupo {{ group.id }}
                        </v-chip>
                        <v-spacer></v-spacer>
                        <v-chip size="small" variant="tonal">
                          <v-icon size="16" class="mr-1">mdi-account-multiple</v-icon>
                          {{ group.members?.length || 0 }}
                        </v-chip>
                      </div>
                      <p class="text-body-2 font-weight-medium mb-0">{{ group.projectTitle }}</p>
                    </div>
                  </v-card>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </div>

    <!-- Teaching Assistant View -->
    <div v-if="roleStore.isTeachingAssistant">
      <!-- Header -->
      <v-row class="mb-3">
        <v-col cols="12">
          <v-card class="elevation-3 role-header-card" :style="{ borderLeft: `6px solid ${roleColor}` }">
            <v-card-text class="pa-4">
              <div class="d-flex align-center">
                <v-avatar size="50" :color="roleColor" class="mr-3">
                  <v-icon size="30" color="white">mdi-account-school</v-icon>
                </v-avatar>
                <div>
                  <h1 class="text-h5 mb-0">Estatísticas de Professor Assistente</h1>
                  <p class="text-caption text-medium-emphasis mb-0">Requisições de Revisão de Notas & Estatísticas de UCs</p>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Stats Cards -->
      <v-row class="mb-3">
        <v-col cols="12" sm="4">
          <v-card class="elevation-2 stat-card h-100" href="/workflow">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-clipboard-list</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ pendingRevisionRequests.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Revisões Pendentes</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="4">
          <v-card class="elevation-2 stat-card h-100" href="/curricular-units">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-book-open-variant</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ curricularUnits.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Unidades Curriculares</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="4">
          <v-card class="elevation-2 stat-card h-100" href="/students">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-account-school</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ totalStudents }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Total de Alunos</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Charts and Lists -->
      <v-row>
        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-chart-donut</v-icon>
              <span>Percentagem de Aprovação Geral</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="d-flex justify-center align-center pa-3" style="height: 250px;">
              <div style="max-width: 250px; max-height: 250px;">
                <Doughnut v-if="passingRateData" :data="passingRateData" :options="chartOptions" />
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-chart-donut</v-icon>
              <span>Status dos Pedidos de Revisão</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="d-flex justify-center align-center pa-3" style="height: 250px;">
              <div style="max-width: 250px; max-height: 250px;">
                <Doughnut v-if="revisionStatusData" :data="revisionStatusData" :options="chartOptions" />
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Pending Reviews List -->
      <v-row class="mt-2">
        <v-col cols="12">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-clipboard-alert</v-icon>
              <span>Pedidos de Revisão Pendentes</span>
              <v-spacer></v-spacer>
              <v-chip size="small" :color="roleColor">{{ pendingRevisionRequests.length }}</v-chip>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-0" style="max-height: 280px; overflow-y: auto;">
              <v-list density="compact" class="py-0">
                <template v-if="pendingRevisionRequests.length > 0">
                  <v-list-item v-for="request in pendingRevisionRequests" :key="request.id">
                    <template v-slot:prepend>
                      <v-icon size="20" color="warning">mdi-alert-circle</v-icon>
                    </template>
                    <v-list-item-title class="text-body-2">
                      {{ request.studentEnrollment?.student?.name || 'Student' }}
                    </v-list-item-title>
                    <v-list-item-subtitle class="text-caption">
                      {{ request.evaluation?.title || 'Evaluation' }} - Grade: {{ request.grade?.toFixed(1) }}
                      <v-chip size="x-small" color="warning" class="ml-1">{{ request.revisionStatus }}</v-chip>
                    </v-list-item-subtitle>
                  </v-list-item>
                </template>
                <v-list-item v-else>
                  <v-list-item-title class="text-center text-medium-emphasis text-caption">Sem pedidos de revisão pendentes</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </div>

    <!-- Main Teacher View -->
    <div v-if="roleStore.isMainTeacher">
      <!-- Header -->
      <v-row class="mb-3">
        <v-col cols="12">
          <v-card class="elevation-3 role-header-card" :style="{ borderLeft: `6px solid ${roleColor}` }">
            <v-card-text class="pa-4">
              <div class="d-flex align-center">
                <v-avatar size="50" :color="roleColor" class="mr-3">
                  <v-icon size="30" color="white">mdi-account-tie</v-icon>
                </v-avatar>
                <div>
                  <h1 class="text-h5 mb-0">Estatísticas de Professor Regente</h1>
                  <p class="text-caption text-medium-emphasis mb-0">Aprovações de Notas & Visão Geral das UCs</p>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Stats Cards -->
      <v-row class="mb-3">
        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/curricular-units">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-book-open-variant</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ curricularUnits.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Unidades Curriculares</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/students">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-account-school</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ totalStudents }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Estudantes</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/Workflow">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-clipboard-check</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ approvalRequests.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Pedidos de Aprovação de Nota</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" color="success" class="mb-2">mdi-chart-line</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ averagePassingRate.toFixed(0) }}%</h3>
              <p class="text-caption text-medium-emphasis mb-0">Percentagem de Aprovação</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Charts -->
      <v-row>
        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-chart-donut</v-icon>
              <span>Percentagem de Aprovação Geral</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="d-flex justify-center align-center pa-3" style="height: 250px;">
              <div style="max-width: 250px; max-height: 250px;">
                <Doughnut v-if="passingRateData" :data="passingRateData" :options="chartOptions" />
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-chart-donut</v-icon>
              <span>Status dos Pedidos de Aprovação</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="d-flex justify-center align-center pa-3" style="height: 250px;">
            <div style="max-width: 250px; max-height: 250px;">
                <Doughnut v-if="revisionStatusData" :data="revisionStatusData" :options="chartOptions" />
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- UCs and Approvals -->
      <v-row class="mt-2">
        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-view-list</v-icon>
              <span>Vista Geral das UCs</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-0" style="max-height: 280px; overflow-y: auto;">
              <v-list density="compact" class="py-0">
                <template v-if="curricularUnitsWithStats.length > 0">
                  <v-list-item v-for="uc in curricularUnitsWithStats" :key="uc.id">
                    <template v-slot:prepend>
                      <v-icon size="20" :color="roleColor">mdi-book</v-icon>
                    </template>
                    <v-list-item-title class="text-body-2">{{ uc.name }} ({{ uc.code }})</v-list-item-title>
                    <v-list-item-subtitle class="text-caption">
                      {{ uc.studentCount }} Estudantes - Aprovados: {{ uc.passingRate?.toFixed(1) }}%
                    </v-list-item-subtitle>
                  </v-list-item>
                </template>
                <v-list-item v-else>
                  <v-list-item-title class="text-center text-medium-emphasis text-caption">Sem Unidades Curriculares</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-clipboard-alert</v-icon>
              <span>Pedidos de Aprovação Pendentes</span>
              <v-spacer></v-spacer>
              <v-chip size="small" :color="roleColor">{{ approvalRequests.length }}</v-chip>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-0" style="max-height: 280px; overflow-y: auto;">
              <v-list density="compact" class="py-0">
                <template v-if="approvalRequests.length > 0">
                  <v-list-item v-for="request in approvalRequests" :key="request.id">
                    <template v-slot:prepend>
                      <v-icon size="20" :color="roleColor">mdi-clipboard-check-outline</v-icon>
                    </template>
                    <v-list-item-title class="text-body-2">
                      {{ request.studentEnrollment?.student?.name || 'Student' }}
                    </v-list-item-title>
                    <v-list-item-subtitle class="text-caption">
                      {{ request.evaluation?.title || 'Evaluation' }} - Nota Sugerida: {{ request.suggestedGrade?.toFixed(1) }}
                      <v-chip size="x-small" :color="roleColor" class="ml-1">{{ request.revisionStatus }}</v-chip>
                    </v-list-item-subtitle>
                  </v-list-item>
                </template>
                <v-list-item v-else>
                  <v-list-item-title class="text-center text-medium-emphasis text-caption">Sem Pedidos de Aprovação Pendentes</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </div>

    <!-- Administrator View -->
    <div v-if="roleStore.isAdministrator">
      <!-- Header -->
      <v-row class="mb-3">
        <v-col cols="12">
          <v-card class="elevation-3 role-header-card" :style="{ borderLeft: `6px solid ${roleColor}` }">
            <v-card-text class="pa-4">
              <div class="d-flex align-center">
                <v-avatar size="50" :color="roleColor" class="mr-3">
                  <v-icon size="30" color="white">mdi-shield-account</v-icon>
                </v-avatar>
                <div>
                  <h1 class="text-h5 mb-0">Estatísticas de Administrador</h1>
                  <p class="text-caption text-medium-emphasis mb-0">Visão Geral do Sistema & Análises</p>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Stats Cards -->
      <v-row class="mb-3">
        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/courses">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-book-multiple</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ courses.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Cursos</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/curricular-units">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-book-open-variant</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ curricularUnits.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Unidades Curriculares</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100" href="/people">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" :color="roleColor" class="mb-2">mdi-account-group</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ people.length }}</h3>
              <p class="text-caption text-medium-emphasis mb-0">Pessoas</p>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="6" sm="3">
          <v-card class="elevation-2 stat-card h-100">
            <v-card-text class="text-center pa-4">
              <v-icon size="32" color="success" class="mb-2">mdi-chart-line</v-icon>
              <h3 class="text-h4 font-weight-bold mb-1">{{ averagePassingRate.toFixed(0) }}%</h3>
              <p class="text-caption text-medium-emphasis mb-0">Taxa de Aprovação</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Charts -->
      <v-row>
        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-chart-donut</v-icon>
              <span>Distribuição de Pessoas</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="d-flex justify-center align-center pa-3" style="height: 250px;">
              <div style="max-width: 250px; max-height: 250px;">
                <Doughnut v-if="peopleDistributionData" :data="peopleDistributionData" :options="chartOptions" />
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-chart-donut</v-icon>
              <span>Taxa de Aprovação Geral</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="d-flex justify-center align-center pa-3" style="height: 250px;">
              <div style="max-width: 250px; max-height: 250px;">
                <Doughnut v-if="passingRateData" :data="passingRateData" :options="chartOptions" />
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Content Lists -->
      <v-row class="mt-2">
        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-book-multiple</v-icon>
              <span>Visão Geral dos Cursos</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-0" style="max-height: 280px; overflow-y: auto;">
              <v-list density="compact" class="py-0">
                <template v-if="courses.length > 0">
                  <v-list-item v-for="course in courses" :key="course.id">
                    <template v-slot:prepend>
                      <v-icon size="20" :color="roleColor">mdi-book</v-icon>
                    </template>
                    <v-list-item-title class="text-body-2">{{ course.name }}</v-list-item-title>
                    <v-list-item-subtitle class="text-caption">{{ course.code }}</v-list-item-subtitle>
                  </v-list-item>
                </template>
                <v-list-item v-else>
                  <v-list-item-title class="text-center text-medium-emphasis text-caption">Sem Cursos Encontrados</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card class="elevation-2 content-card">
            <v-card-title class="text-subtitle-1 pa-3 d-flex align-center" :style="{ backgroundColor: roleColor + '15' }">
              <v-icon class="mr-2" :color="roleColor">mdi-chart-bar</v-icon>
              <span>Estatísticas Rápidas</span>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text class="pa-0" style="max-height: 280px; overflow-y: auto;">
              <v-list density="compact" class="py-0">
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon size="20" color="info">mdi-account-school</v-icon>
                  </template>
                  <v-list-item-title class="text-body-2">Estudantes</v-list-item-title>
                  <v-list-item-subtitle class="text-caption">{{ studentCount }} Estudantes inscritos</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon size="20" color="success">mdi-account-tie</v-icon>
                  </template>
                  <v-list-item-title class="text-body-2">Professores Assistentes</v-list-item-title>
                  <v-list-item-subtitle class="text-caption">{{ teacherCount }} assistentes</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon size="20" color="warning">mdi-account-star</v-icon>
                  </template>
                  <v-list-item-title class="text-body-2">Professores Regentes</v-list-item-title>
                  <v-list-item-subtitle class="text-caption">{{ regentTeacherCount }} professores regentes</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <template v-slot:prepend>
                    <v-icon size="20" :color="roleColor">mdi-check-all</v-icon>
                  </template>
                  <v-list-item-title class="text-body-2">Estudantes Aprovados</v-list-item-title>
                  <v-list-item-subtitle class="text-caption">{{ passedStudentsCount }} estudantes aprovados</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoleStore } from '@/stores/role'
import { useAppearanceStore } from '@/stores/appearance'
import RemoteServices from '@/services/RemoteService'
import { Doughnut } from 'vue-chartjs'
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  type ChartData,
  type ChartOptions as ChartJSOptions
} from 'chart.js'

// Register Chart.js components
ChartJS.register(ArcElement, Tooltip, Legend)

const roleStore = useRoleStore()
const appearanceStore = useAppearanceStore()

// Student data
const studentProfile = ref<any>(null)
const enrolledUCs = ref<any[]>([])
const passedUCs = ref<any[]>([])
const studentGroups = ref<any[]>([])
const upcomingEvaluations = ref<any[]>([])

// Common data
const curricularUnits = ref<any[]>([])
const courses = ref<any[]>([])
const people = ref<any[]>([])
const revisionRequests = ref<any[]>([])

// Computed properties
const cardColor = computed(() => appearanceStore.isDarkTheme ? 'surface' : 'white')

const roleColor = computed(() => {
  if (roleStore.isStudent) return '#4CAF50' // green
  if (roleStore.isTeachingAssistant) return '#2196F3' // blue
  if (roleStore.isMainTeacher) return '#F44336' // red
  if (roleStore.isAdministrator) return '#9C27B0' // purple
  return '#757575' // grey
})

const totalStudents = computed(() => {
  let studentSet = new Set<number>()
  curricularUnits.value.forEach((uc: any) => {
    if (uc.enrollments) {
      uc.enrollments.forEach((enrollment: any) => {
        if (enrollment.student?.id) {
          studentSet.add(enrollment.student.id)
        }
      })
    }
  })
  return studentSet.size
})

const pendingRevisionRequests = computed(() => {
  return revisionRequests.value.filter((req: any) => 
    req.revisionStatus === 'REQUESTED' // For teaching assistants: newly requested revisions
  )
})

const approvalRequests = computed(() => {
  return revisionRequests.value.filter((req: any) => 
    req.revisionStatus === 'TEACHER_SUBMITTED' // For regent teachers: revisions submitted by assistants awaiting approval
  )
})

const curricularUnitsWithStats = computed(() => {
  return curricularUnits.value.map((uc: any) => {
    const enrollments = uc.enrollments || []
    const approved = enrollments.filter((e: any) => e.status === 'APPROVED').length
    const total = enrollments.length
    return {
      ...uc,
      studentCount: total,
      passingRate: total > 0 ? (approved / total) * 100 : 0
    }
  })
})

const averagePassingRate = computed(() => {
  if (curricularUnits.value.length === 0) return 0
  
  let totalApproved = 0
  let totalEnrollments = 0
  
  curricularUnits.value.forEach((uc: any) => {
    const enrollments = uc.enrollments || []
    totalEnrollments += enrollments.length
    totalApproved += enrollments.filter((e: any) => e.status === 'APPROVED').length
  })
  
  return totalEnrollments > 0 ? (totalApproved / totalEnrollments) * 100 : 0
})

const studentCount = computed(() => people.value.filter((p: any) => p.type === 'STUDENT').length)
const teacherCount = computed(() => people.value.filter((p: any) => p.type === 'TEACHING_ASSISTANT').length)
const regentTeacherCount = computed(() => people.value.filter((p: any) => p.type === 'MAIN_TEACHER').length)

const passedStudentsCount = computed(() => {
  let passedSet = new Set<number>()
  curricularUnits.value.forEach((uc: any) => {
    if (uc.enrollments) {
      uc.enrollments.forEach((enrollment: any) => {
        if (enrollment.status === 'APPROVED' && enrollment.student?.id) {
          passedSet.add(enrollment.student.id)
        }
      })
    }
  })
  return passedSet.size
})

// Chart data
const passingRateData = computed(() => {
  if (curricularUnits.value.length === 0) return null
  
  let approved = 0
  let failed = 0
  let enrolled = 0
  
  curricularUnits.value.forEach((uc: any) => {
    const enrollments = uc.enrollments || []
    enrollments.forEach((e: any) => {
      if (e.status === 'APPROVED') approved++
      else if (e.status === 'FAILED') failed++
      else if (e.status === 'ENROLLED') enrolled++
    })
  })
  
  return {
    labels: ['Approved', 'Failed', 'Enrolled'],
    datasets: [{
      data: [approved, failed, enrolled],
      backgroundColor: appearanceStore.isDarkTheme 
        ? ['#66BB6A', '#EF5350', '#42A5F5']
        : ['#4CAF50', '#F44336', '#2196F3'],
      borderWidth: 2,
      borderColor: appearanceStore.isDarkTheme ? '#424242' : '#ffffff'
    }]
  }
})

const revisionStatusData = computed(() => {
  if (revisionRequests.value.length === 0) return null
  
  const statusCounts: Record<string, number> = {}
  revisionRequests.value.forEach((req: any) => {
    const status = req.revisionStatus || 'NONE'
    statusCounts[status] = (statusCounts[status] || 0) + 1
  })
  
  return {
    labels: Object.keys(statusCounts),
    datasets: [{
      data: Object.values(statusCounts) as number[],
      backgroundColor: appearanceStore.isDarkTheme
        ? ['#FFA726', '#66BB6A', '#EF5350', '#42A5F5']
        : ['#FF9800', '#4CAF50', '#F44336', '#2196F3'],
      borderWidth: 2,
      borderColor: appearanceStore.isDarkTheme ? '#424242' : '#ffffff'
    }]
  }
})

const approvalStatusData = computed(() => {
  if (approvalRequests.value.length === 0) return null
  
  const pending = approvalRequests.value.filter((r: any) => r.revisionStatus === 'PENDING_REGENT_APPROVAL').length
  const approved = approvalRequests.value.filter((r: any) => r.revisionStatus === 'APPROVED').length
  const rejected = approvalRequests.value.filter((r: any) => r.revisionStatus === 'REJECTED').length
  
  return {
    labels: ['Pending', 'Approved', 'Rejected'],
    datasets: [{
      data: [pending, approved, rejected],
      backgroundColor: appearanceStore.isDarkTheme
        ? ['#FFA726', '#66BB6A', '#EF5350']
        : ['#FF9800', '#4CAF50', '#F44336'],
      borderWidth: 2,
      borderColor: appearanceStore.isDarkTheme ? '#424242' : '#ffffff'
    }]
  }
})

const peopleDistributionData = computed(() => {
  if (people.value.length === 0) return null
  
  const students = studentCount.value
  const teachers = teacherCount.value
  const regents = regentTeacherCount.value
  const admins = people.value.filter((p: any) => p.type === 'ADMINISTRATOR').length
  
  return {
    labels: ['Students', 'Teaching Assistants', 'Regent Teachers', 'Administrators'],
    datasets: [{
      data: [students, teachers, regents, admins],
      backgroundColor: appearanceStore.isDarkTheme
        ? ['#42A5F5', '#66BB6A', '#FFA726', '#AB47BC']
        : ['#2196F3', '#4CAF50', '#FF9800', '#9C27B0'],
      borderWidth: 2,
      borderColor: appearanceStore.isDarkTheme ? '#424242' : '#ffffff'
    }]
  }
})

const chartOptions = computed<ChartJSOptions<'doughnut'>>(() => ({
  responsive: true,
  maintainAspectRatio: true,
  plugins: {
    legend: {
      position: 'bottom',
      labels: {
        color: appearanceStore.isDarkTheme ? '#ffffff' : '#424242',
        padding: 15,
        font: {
          size: 12
        }
      }
    },
    tooltip: {
      backgroundColor: appearanceStore.isDarkTheme ? '#424242' : '#ffffff',
      titleColor: appearanceStore.isDarkTheme ? '#ffffff' : '#424242',
      bodyColor: appearanceStore.isDarkTheme ? '#ffffff' : '#424242',
      borderColor: appearanceStore.isDarkTheme ? '#757575' : '#e0e0e0',
      borderWidth: 1
    }
  }
}))

// Helper functions
const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('pt-PT', { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric' 
  })
}

// Data loading functions
const loadStudentData = async () => {
  try {
    appearanceStore.loading = true
    const studentId = 'ist109685'
    
    // Load all people to find the student
    const allPeople = await RemoteServices.getPeople()
    studentProfile.value = allPeople.find((p: any) => p.istId === studentId)
    
    // Load all curricular units
    const allUCs = await RemoteServices.getCurricularUnits()
    
    // Load enrollments for each UC and filter for this student
    for (const uc of allUCs) {
      try {
        const enrollments = await RemoteServices.getEnrollmentsByCurricularUnit(uc.id!)
        const studentEnrollment = enrollments.find((e: any) => e.student?.istId === studentId)
        
        if (studentEnrollment) {
          enrolledUCs.value.push({
            ...studentEnrollment,
            curricularUnit: uc
          })
          
          if (studentEnrollment.status === 'APPROVED') {
            passedUCs.value.push({
              ...studentEnrollment,
              curricularUnit: uc
            })
          }
        }
      } catch (err) {
        console.error(`Error loading enrollments for UC ${uc.id}:`, err)
      }
    }
    
    // Load upcoming evaluations
    const allEvaluations = await RemoteServices.getAllEvaluations()
    const now = new Date()
    upcomingEvaluations.value = allEvaluations
      .filter((e: any) => new Date(e.date) > now)
      .sort((a: any, b: any) => new Date(a.date).getTime() - new Date(b.date).getTime())
      .slice(0, 10)
    
    // Load projects and groups for student
    if (studentProfile.value?.id) {
      for (const enrollment of enrolledUCs.value) {
        try {
          const projects = await RemoteServices.getProjectsByCurricularUnit(enrollment.curricularUnit.id!)
          for (const project of projects) {
            const group = await RemoteServices.getStudentGroup(project.id!, studentProfile.value.id!)
            if (group) {
              studentGroups.value.push({
                ...group,
                projectTitle: project.title
              })
            }
          }
        } catch (err) {
          console.error('Error loading projects:', err)
        }
      }
    }
  } catch (error) {
    console.error('Error loading student data:', error)
  } finally {
    appearanceStore.loading = false
  }
}

const loadTeacherData = async () => {
  try {
    appearanceStore.loading = true
    
    // Load curricular units
    curricularUnits.value = await RemoteServices.getCurricularUnits()
    
    // Load enrollments for each UC
    for (const uc of curricularUnits.value) {
      try {
        const enrollments = await RemoteServices.getEnrollmentsByCurricularUnit(uc.id!)
        uc.enrollments = enrollments
      } catch (err) {
        console.error(`Error loading enrollments for UC ${uc.id}:`, err)
        uc.enrollments = []
      }
    }
    
    // Load revision requests
    revisionRequests.value = await RemoteServices.getRevisionRequests()
  } catch (error) {
    console.error('Error loading teacher data:', error)
  } finally {
    appearanceStore.loading = false
  }
}

const loadAdminData = async () => {
  try {
    appearanceStore.loading = true
    
    // Load all data
    courses.value = await RemoteServices.getCourses()
    curricularUnits.value = await RemoteServices.getCurricularUnits()
    people.value = await RemoteServices.getPeople()
    
    // Load enrollments for each UC
    for (const uc of curricularUnits.value) {
      try {
        const enrollments = await RemoteServices.getEnrollmentsByCurricularUnit(uc.id!)
        uc.enrollments = enrollments
      } catch (err) {
        console.error(`Error loading enrollments for UC ${uc.id}:`, err)
        uc.enrollments = []
      }
    }
  } catch (error) {
    console.error('Error loading admin data:', error)
  } finally {
    appearanceStore.loading = false
  }
}

// Function to clear all data
const clearAllData = () => {
  studentProfile.value = null
  enrolledUCs.value = []
  passedUCs.value = []
  studentGroups.value = []
  upcomingEvaluations.value = []
  curricularUnits.value = []
  courses.value = []
  people.value = []
  revisionRequests.value = []
}

// Function to load data based on current role
const loadDataForCurrentRole = async () => {
  clearAllData()
  
  if (roleStore.isStudent) {
    await loadStudentData()
  } else if (roleStore.isTeachingAssistant) {
    await loadTeacherData()
  } else if (roleStore.isMainTeacher) {
    await loadTeacherData()
  } else if (roleStore.isAdministrator) {
    await loadAdminData()
  }
}

// Watch for role changes
watch(() => roleStore.currentActiveRole, async (newRole, oldRole) => {
  if (newRole !== oldRole && newRole) {
    await loadDataForCurrentRole()
  }
})

onMounted(async () => {
  await loadDataForCurrentRole()
})
</script>

<style scoped>
.stat-card {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2) !important;
}
</style>

