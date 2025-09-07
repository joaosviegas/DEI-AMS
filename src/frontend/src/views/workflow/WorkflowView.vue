<template>
  <v-container>
    <v-row class="mb-2">
      <v-col>
        <h2 class="text-h4 font-weight-bold">
          <v-icon large class="mr-2">mdi-clipboard-flow</v-icon>
          Revisão de Avaliações
        </h2>
        <p class="text-subtitle-1 text-medium-emphasis">
          {{ getRoleDescription() }}
        </p>
      </v-col>
    </v-row>

    <v-card v-if="loading" class="mb-6">
      <v-card-text class="d-flex justify-center align-center pa-6">
        <v-progress-circular indeterminate color="primary" class="mr-3"></v-progress-circular>
        <span>Carregando informações de revisões...</span>
      </v-card-text>
    </v-card>

    <div v-else>
      <!-- Summary Card -->
      <v-card class="mb-6 mt-10" :color="getSummaryColor()">
        <v-card-item>
          <template v-slot:prepend>
            <v-avatar :color="getSummaryIconColor()" class="text-white">
              <v-icon>{{ getSummaryIcon() }}</v-icon>
            </v-avatar>
          </template>

          <v-card-title>{{ getSummaryTitle() }}</v-card-title>
          <v-card-subtitle>{{ getSummarySubtitle() }}</v-card-subtitle>
        </v-card-item>
      </v-card>

      <!-- Phase Tabs -->
      <v-card>
        <v-tabs v-model="activeTab" align-tabs="center" color="primary">
          <v-tab value="phase1" prepend-icon="mdi-account-school">
            {{ isStudent ? 'Minhas Avaliações⠀' : 'Solicitações⠀' }}
            <v-badge v-if="getPhaseCount('phase1') > 0" :content="getPhaseCount('phase1')" color="primary" class="ml-2"></v-badge>
          </v-tab>
          <v-tab v-if="isTeacher" value="phase2" prepend-icon="mdi-account">
            Revisão Docente⠀
            <v-badge v-if="getPhaseCount('phase2') > 0" :content="getPhaseCount('phase2')" color="warning" class="ml-2"></v-badge>
          </v-tab>
          <v-tab v-if="isMainTeacher" value="phase3" prepend-icon="mdi-account-tie">
            Aprovação Final⠀
            <v-badge v-if="getPhaseCount('phase3') > 0" :content="getPhaseCount('phase3')" color="success" class="ml-2"></v-badge>
          </v-tab>
        </v-tabs>

        <v-window v-model="activeTab">
          <!-- Phase 1: Student Requests -->
          <v-window-item value="phase1">
            <v-card-title class="d-flex align-center pb-0">
              <v-icon color="green" class="mr-2">mdi-account-school</v-icon>
              {{ isStudent ? 'Minhas Avaliações' : 'Solicitações de Revisão' }}
            </v-card-title>

            <!-- Filter and Search Controls -->
            <v-card flat class="ma-4 mb-0">
              <v-card-text>
                <v-row>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model="phase1Search"
                      label="Pesquisar avaliações"
                      prepend-inner-icon="mdi-magnify"
                      clearable
                      variant="outlined"
                      density="compact"
                      hide-details
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" md="3">
                    <v-select
                      v-model="selectedCurricularUnit"
                      :items="curricularUnits"
                      label="Filtrar por UC"
                      variant="outlined"
                      density="compact"
                      hide-details
                    ></v-select>
                  </v-col>
                  <v-col cols="12" md="3">
                    <v-select
                      v-model="selectedEvaluationType"
                      :items="evaluationTypes"
                      label="Tipo de avaliação"
                      variant="outlined"
                      density="compact"
                      hide-details
                    ></v-select>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>

            <v-data-table
              :headers="phase1Headers"
              :items="phase1Items"
              :search="phase1Search"
              class="elevation-0"
              :loading="loading"
              :no-data-text="isStudent ? 'Não possui avaliações registadas.' : 'Nenhuma avaliação disponível para revisão.'"
            >
              <template v-slot:[`item.evaluationType`]="{ item }">
                <v-chip 
                  :color="item.evaluationType === 'TEST' ? 'green' : 'blue'" 
                  size="small"
                >
                  {{ item.evaluationType === 'TEST' ? 'Teste' : 'Projeto' }}
                </v-chip>
              </template>

              <template v-slot:[`item.currentGrade`]="{ item }">
                <div class="d-flex align-center">
                  <span class="font-weight-medium">{{ formatGrade(item.currentGrade) }}/20</span>
                  <v-chip 
                    :color="item.currentGrade >= 10 ? 'green' : 'red'"
                    size="x-small"
                    class="ml-2"
                  >
                    {{ item.currentGrade >= 10 ? 'Positiva' : 'Negativa' }}
                  </v-chip>
                </div>
              </template>

              <template v-slot:[`item.status`]="{ item }">
                <v-chip :color="getStatusColor(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </v-chip>
              </template>

              <template v-slot:[`item.actions`]="{ item }">
                <div class="d-flex justify-end align-center">
                  <v-tooltip location="top" text="Ver detalhes">
                    <template v-slot:activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-eye"
                        variant="text"
                        size="small"
                        @click="viewDetails(item)"
                      ></v-btn>
                    </template>
                  </v-tooltip>
                  
                  <v-tooltip 
                    v-if="canRequestRevision(item)" 
                    location="top" 
                    text="Solicitar revisão"
                  >
                    <template v-slot:activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-clipboard-edit"
                        variant="text"
                        size="small"
                        color="orange"
                        @click="openRevisionRequestDialog(item)"
                      ></v-btn>
                    </template>
                  </v-tooltip>
                </div>
              </template>
            </v-data-table>
          </v-window-item>

          <!-- Phase 2: Teacher Review -->
          <v-window-item v-if="isTeacher" value="phase2">
            <v-card-title class="d-flex align-center pb-0">
              <v-icon color="blue" class="mr-2">mdi-account</v-icon>
              Revisão Docente
            </v-card-title>

            <!-- Filter and Search Controls -->
            <v-card flat class="ma-4 mb-0">
              <v-card-text>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="phase2Search"
                      label="Pesquisar revisões"
                      prepend-inner-icon="mdi-magnify"
                      clearable
                      variant="outlined"
                      density="compact"
                      hide-details
                    ></v-text-field>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>

            <v-data-table
              :headers="phase2Headers"
              :items="phase2Items"
              :search="phase2Search"
              class="elevation-0"
              :loading="loading"
              no-data-text="Nenhuma revisão pendente."
            >
              <template v-slot:[`item.evaluationType`]="{ item }">
                <v-chip 
                  :color="item.evaluationType === 'TEST' ? 'green' : 'blue'" 
                  size="small"
                >
                  {{ item.evaluationType === 'TEST' ? 'Teste' : 'Projeto' }}
                </v-chip>
              </template>

              <template v-slot:[`item.currentGrade`]="{ item }">
                <span class="font-weight-medium">{{ formatGrade(item.currentGrade) }}/20</span>
              </template>

              <template v-slot:[`item.requestedAt`]="{ item }">
                {{ formatDateTime(item.requestedAt) }}
              </template>

              <template v-slot:[`item.actions`]="{ item }">
                <div class="d-flex justify-end align-center">
                  <v-tooltip location="top" text="Ver detalhes">
                    <template v-slot:activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-eye"
                        variant="text"
                        size="small"
                        @click="viewDetails(item)"
                      ></v-btn>
                    </template>
                  </v-tooltip>
                  
                  <v-tooltip 
                    v-if="canReviewRevision(item)" 
                    location="top" 
                    text="Revisar avaliação"
                  >
                    <template v-slot:activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-pencil"
                        variant="text"
                        size="small"
                        color="warning"
                        @click="openTeacherReviewDialog(item)"
                      ></v-btn>
                    </template>
                  </v-tooltip>
                </div>
              </template>
            </v-data-table>
          </v-window-item>

          <!-- Phase 3: Final Approval -->
          <v-window-item v-if="isMainTeacher" value="phase3">
            <v-card-title class="d-flex align-center pb-0">
              <v-icon color="red" class="mr-2">mdi-account-tie</v-icon>
              Aprovação Final
            </v-card-title>

            <!-- Filter and Search Controls -->
            <v-card flat class="ma-4 mb-0">
              <v-card-text>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="phase3Search"
                      label="Pesquisar aprovações"
                      prepend-inner-icon="mdi-magnify"
                      clearable
                      variant="outlined"
                      density="compact"
                      hide-details
                    ></v-text-field>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>

            <v-data-table
              :headers="phase3Headers"
              :items="phase3Items"
              :search="phase3Search"
              class="elevation-0"
              :loading="loading"
              no-data-text="Nenhuma revisão aguardando aprovação final."
            >
              <template v-slot:[`item.evaluationType`]="{ item }">
                <v-chip 
                  :color="item.evaluationType === 'TEST' ? 'green' : 'blue'" 
                  size="small"
                >
                  {{ item.evaluationType === 'TEST' ? 'Teste' : 'Projeto' }}
                </v-chip>
              </template>

              <template v-slot:[`item.currentGrade`]="{ item }">
                <span class="font-weight-medium">{{ formatGrade(item.currentGrade) }}/20</span>
              </template>

              <template v-slot:[`item.suggestedGrade`]="{ item }">
                <div class="d-flex align-center">
                  <span class="font-weight-medium">{{ formatGrade(item.suggestedGrade) }}/20</span>
                  <v-icon 
                    :color="item.suggestedGrade > item.currentGrade ? 'green' : item.suggestedGrade < item.currentGrade ? 'red' : 'grey'"
                    size="small"
                    class="ml-1"
                  >
                    {{ item.suggestedGrade > item.currentGrade ? 'mdi-arrow-up' : item.suggestedGrade < item.currentGrade ? 'mdi-arrow-down' : 'mdi-minus' }}
                  </v-icon>
                </div>
              </template>

              <template v-slot:[`item.reviewedAt`]="{ item }">
                {{ formatDateTime(item.reviewedAt) }}
              </template>

              <template v-slot:[`item.actions`]="{ item }">
                <div class="d-flex justify-end align-center">
                  <v-tooltip location="top" text="Ver detalhes">
                    <template v-slot:activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-eye"
                        variant="text"
                        size="small"
                        @click="viewDetails(item)"
                      ></v-btn>
                    </template>
                  </v-tooltip>
                  
                  <v-tooltip 
                    v-if="canFinalApprove(item)" 
                    location="top" 
                    text="Aprovar/Rejeitar"
                  >
                    <template v-slot:activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-check-decagram"
                        variant="text"
                        size="small"
                        color="success"
                        @click="openFinalApprovalDialog(item)"
                      ></v-btn>
                    </template>
                  </v-tooltip>
                </div>
              </template>
            </v-data-table>
          </v-window-item>
        </v-window>
      </v-card>
    </div>

    <!-- Revision Request Dialog (Phase 1) -->
    <v-dialog v-model="revisionRequestDialog" max-width="600px">
      <v-card v-if="selectedItem">
        <v-card-title class="bg-primary text-white">
          <v-icon start>mdi-clipboard-edit</v-icon>
          Solicitar Revisão de Nota
        </v-card-title>
        
        <v-card-text class="pa-4 pt-6">
          <div class="mb-4">
            <strong>Aluno:</strong> {{ selectedItem.studentName }}<br>
            <strong>Avaliação:</strong> {{ selectedItem.evaluationTitle }}<br>
            <strong>Nota Atual:</strong> {{ formatGrade(selectedItem.currentGrade) }}/20
          </div>
          
          <p class="mb-4">
            Deseja solicitar uma revisão da sua nota para esta avaliação?
          </p>
          
          <v-form ref="revisionRequestForm" v-model="revisionRequestValid">
            <v-textarea
              v-model="revisionReason"
              label="Motivo da solicitação"
              placeholder="Descreva o motivo pelo qual solicita a revisão da nota..."
              rows="4"
              :rules="[v => !!v || 'Motivo é obrigatório']"
              required
            ></v-textarea>
          </v-form>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="revisionRequestDialog = false">
            Cancelar
          </v-btn>
          <v-btn 
            color="primary" 
            :disabled="!revisionRequestValid"
            @click="submitRevisionRequest"
            :loading="submitting"
          >
            Solicitar Revisão
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Teacher Review Dialog (Phase 2) -->
    <v-dialog v-model="teacherReviewDialog" max-width="700px">
      <v-card v-if="selectedItem">
        <v-card-title class="bg-warning text-white">
          <v-icon start>mdi-pencil</v-icon>
          Revisão Docente
        </v-card-title>
        
        <v-card-text class="pa-4 pt-6">
          <div class="mb-4">
            <strong>Aluno:</strong> {{ selectedItem.studentName }}<br>
            <strong>Avaliação:</strong> {{ selectedItem.evaluationTitle }}<br>
            <strong>Nota Atual:</strong> {{ formatGrade(selectedItem.currentGrade) }}/20<br>
            <strong>Motivo da Solicitação:</strong> {{ selectedItem.reason }}
            <div v-if="selectedItem.finalJustification" class="mt-2">
              <strong>Feedback do Regente:</strong> {{ selectedItem.finalJustification }}
            </div>
          </div>
          
          <v-alert
            v-if="selectedItem.finalJustification"
            density="comfortable"
            type="warning"
            variant="outlined"
            class="mb-4"
          >
            Esta revisão foi rejeitada pelo Regente da UC. Pode submeter uma nova proposta considerando o feedback fornecido.
          </v-alert>
          
          <v-alert
            density="comfortable"
            type="info"
            variant="outlined"
            class="mb-4"
          >
            Como docente, pode alterar a nota ou mantê-la como está. A decisão será enviada para aprovação final.
          </v-alert>
          
          <v-form ref="teacherReviewForm" v-model="teacherReviewValid">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.number="suggestedGrade"
                  label="Nova Nota Sugerida"
                  type="number"
                  min="0"
                  max="20"
                  step="0.1"
                  suffix="/20"
                  :rules="gradeRules"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            
            <v-textarea
              v-model="teacherJustification"
              label="Justificação da Decisão"
              placeholder="Justifique a decisão de revisão..."
              rows="3"
              :rules="[v => !!v || 'Justificação é obrigatória']"
              required
            ></v-textarea>
          </v-form>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="teacherReviewDialog = false">
            Cancelar
          </v-btn>
          <v-btn 
            color="warning" 
            :disabled="!teacherReviewValid"
            @click="submitTeacherReview"
            :loading="submitting"
          >
            Enviar para Aprovação
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Final Approval Dialog (Phase 3) -->
    <v-dialog v-model="finalApprovalDialog" max-width="700px">
      <v-card v-if="selectedItem">
        <v-card-title class="bg-success text-white">
          <v-icon start>mdi-check-decagram</v-icon>
          Aprovação Final
        </v-card-title>
        
        <v-card-text class="pa-4 pt-6">
          <div class="mb-4">
            <strong>Aluno:</strong> {{ selectedItem.studentName }}<br>
            <strong>Avaliação:</strong> {{ selectedItem.evaluationTitle }}<br>
            <strong>Nota Original:</strong> {{ formatGrade(selectedItem.currentGrade) }}/20<br>
            <strong>Nota Sugerida:</strong> {{ formatGrade(selectedItem.suggestedGrade) }}/20<br>
            <strong>Justificação Docente:</strong> {{ selectedItem.teacherJustification }}
          </div>
          
          <v-alert
            density="comfortable"
            type="success"
            variant="outlined"
            class="mb-4"
          >
            Como Regente da UC, tem a decisão final sobre esta revisão.
          </v-alert>
          
          <v-form ref="finalApprovalForm" v-model="finalApprovalValid">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.number="finalGrade"
                  label="Nota Final"
                  type="number"
                  min="0"
                  max="20"
                  step="0.1"
                  suffix="/20"
                  :rules="gradeRules"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            
            <v-textarea
              v-model="finalJustification"
              label="Justificação da Decisão Final"
              placeholder="Justifique a decisão final..."
              rows="3"
              :rules="[v => !!v || 'Justificação é obrigatória']"
              required
            ></v-textarea>
          </v-form>
        </v-card-text>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="finalApprovalDialog = false">
            Cancelar
          </v-btn>
          <v-btn 
            color="red" 
            variant="outlined"
            @click="rejectFinalApproval"
            :loading="submitting"
          >
            Rejeitar e Retornar
          </v-btn>
          <v-btn 
            color="success" 
            :disabled="!finalApprovalValid"
            @click="approveFinalGrade"
            :loading="submitting"
          >
            Aprovar Nota Final
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Details Dialog -->
    <v-dialog v-model="detailsDialog" max-width="800px">
      <v-card v-if="selectedItem">
        <v-card-title class="bg-blue text-white d-flex justify-space-between align-center">
          <div class="d-flex align-center">
            <v-icon class="mr-2" color="white">mdi-clipboard-text</v-icon>
            <span class="font-weight-medium">Detalhes da Revisão</span>
          </div>
          <v-btn icon variant="text" color="white" @click="detailsDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>
        
        <v-card-text class="pa-4 pt-6">
          <v-row>
            <v-col cols="12" md="6">
              <v-list>
                <v-list-item>
                  <v-list-item-title>Aluno</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedItem.studentName }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>IST ID</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedItem.studentIstId }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Avaliação</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedItem.evaluationTitle }}</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Unidade Curricular</v-list-item-title>
                  <v-list-item-subtitle>{{ selectedItem.curricularUnitName }}</v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
            
            <v-col cols="12" md="6">
              <v-list>
                <v-list-item>
                  <v-list-item-title>Nota Original</v-list-item-title>
                  <v-list-item-subtitle>{{ formatGrade(selectedItem.currentGrade) }}/20</v-list-item-subtitle>
                </v-list-item>
                <v-list-item v-if="selectedItem.suggestedGrade">
                  <v-list-item-title>Nota Sugerida</v-list-item-title>
                  <v-list-item-subtitle>{{ formatGrade(selectedItem.suggestedGrade) }}/20</v-list-item-subtitle>
                </v-list-item>
                <v-list-item v-if="selectedItem.finalGrade">
                  <v-list-item-title>Nota Final</v-list-item-title>
                  <v-list-item-subtitle>{{ formatGrade(selectedItem.finalGrade) }}/20</v-list-item-subtitle>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title>Status</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip :color="getStatusColor(selectedItem.status)" size="small">
                      {{ getStatusText(selectedItem.status) }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-col>
          </v-row>
                    
          <div v-if="selectedItem.reason">
            <h4 class="mb-2">Motivo da Solicitação:</h4>
            <p class="mb-4">{{ selectedItem.reason }}</p>
          </div>
          
          <div v-if="selectedItem.teacherJustification">
            <h4 class="mb-2">Justificação Docente:</h4>
            <p class="mb-4">{{ selectedItem.teacherJustification }}</p>
          </div>
          
          <div v-if="selectedItem.finalJustification">
            <h4 class="mb-2">Justificação Final:</h4>
            <p>{{ selectedItem.finalJustification }}</p>
          </div>
        </v-card-text>
        
        <v-divider></v-divider>
        
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" prepend-icon="mdi-close" @click="detailsDialog = false">
            Fechar
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoleStore } from '../../stores/role'
import RemoteService from '../../services/RemoteService'

// States
const loading = ref(true)
const submitting = ref(false)
const activeTab = ref('phase1')
const roleStore = useRoleStore()

// Search filters
const phase1Search = ref('')
const phase2Search = ref('')
const phase3Search = ref('')

// Filter options for Phase 1
const selectedCurricularUnit = ref('all')
const selectedEvaluationType = ref('all')
const curricularUnits = ref<Array<{value: string, title: string}>>([])
const evaluationTypes = [
  { value: 'all', title: 'Todos os Tipos' },
  { value: 'TEST', title: 'Teste' },
  { value: 'PROJECT', title: 'Projeto' }
]

// Data
const revisionRequests = ref<any[]>([])
const selectedItem = ref<any>(null)

// Dialog states
const detailsDialog = ref(false)
const revisionRequestDialog = ref(false)
const teacherReviewDialog = ref(false)
const finalApprovalDialog = ref(false)

// Form states
const revisionRequestForm = ref()
const teacherReviewForm = ref()
const finalApprovalForm = ref()
const revisionRequestValid = ref(false)
const teacherReviewValid = ref(false)
const finalApprovalValid = ref(false)

// Form data
const revisionReason = ref('')
const suggestedGrade = ref<number | null>(null)
const teacherJustification = ref('')
const finalGrade = ref<number | null>(null)
const finalJustification = ref('')

// Computed properties for role-based access
const isStudent = computed(() => roleStore.isStudent)
const isTeacher = computed(() => roleStore.isMainTeacher || roleStore.isTeachingAssistant)
const isMainTeacher = computed(() => roleStore.isMainTeacher)

// Grade validation rules
const gradeRules = [
  (v: any) => v !== null && v !== undefined && v !== '' || 'Nota é obrigatória',
  (v: any) => (v >= 0 && v <= 20) || 'Nota deve estar entre 0 e 20',
]

// Table headers
const phase1Headers = [
  { title: 'Aluno', key: 'studentName', align: 'start' as const, sortable: true },
  { title: 'IST ID', key: 'studentIstId', align: 'start' as const, sortable: true },
  { title: 'Avaliação', key: 'evaluationTitle', align: 'start' as const, sortable: true },
  { title: 'Tipo', key: 'evaluationType', align: 'center' as const, sortable: true },
  { title: 'Nota Atual', key: 'currentGrade', align: 'center' as const, sortable: true },
  { title: 'Status', key: 'status', align: 'center' as const, sortable: false },
  { title: 'Ações', key: 'actions', align: 'end' as const, sortable: false },
]

const phase2Headers = [
  { title: 'Aluno', key: 'studentName', align: 'start' as const, sortable: true },
  { title: 'Avaliação', key: 'evaluationTitle', align: 'start' as const, sortable: true },
  { title: 'Tipo', key: 'evaluationType', align: 'center' as const, sortable: true },
  { title: 'Nota Atual', key: 'currentGrade', align: 'center' as const, sortable: true },
  { title: 'Solicitado em', key: 'requestedAt', align: 'center' as const, sortable: true },
  { title: 'Ações', key: 'actions', align: 'end' as const, sortable: false },
]

const phase3Headers = [
  { title: 'Aluno', key: 'studentName', align: 'start' as const, sortable: true },
  { title: 'Avaliação', key: 'evaluationTitle', align: 'start' as const, sortable: true },
  { title: 'Tipo', key: 'evaluationType', align: 'center' as const, sortable: true },
  { title: 'Nota Original', key: 'currentGrade', align: 'center' as const, sortable: true },
  { title: 'Nota Sugerida', key: 'suggestedGrade', align: 'center' as const, sortable: true },
  { title: 'Revisto em', key: 'reviewedAt', align: 'center' as const, sortable: true },
  { title: 'Ações', key: 'actions', align: 'end' as const, sortable: false },
]

// Computed data for each phase
const phase1Items = computed(() => {
  // Phase 1: Show ALL graded evaluations - students can potentially request revision
  // This includes evaluations with no revision requests and those with revision requests
  let items = revisionRequests.value;
  
  // Apply curricular unit filter
  if (selectedCurricularUnit.value !== 'all') {
    items = items.filter(item => item.curricularUnitName === selectedCurricularUnit.value);
  }
  
  // Apply evaluation type filter
  if (selectedEvaluationType.value !== 'all') {
    items = items.filter(item => item.evaluationType === selectedEvaluationType.value);
  }
  
  return items;
})

const phase2Items = computed(() => {
  // Phase 2: Show evaluations that have revision requests and need teacher review
  // This includes new revision requests and items rejected from Phase 3
  return revisionRequests.value.filter(item => {
    const status = item.status || item.revisionStatus;
    return status === 'REVISION_REQUESTED' || status === 'REQUESTED';
  });
})

const phase3Items = computed(() => {
  // Phase 3: Show evaluations that teachers have reviewed and need final approval
  return revisionRequests.value.filter(item => {
    const status = item.status || item.revisionStatus;
    return status === 'AWAITING_FINAL_APPROVAL' || status === 'TEACHER_SUBMITTED';
  });
})

// Helper functions
const formatGrade = (grade: number) => {
  return grade?.toFixed(1) || '0.0'
}

const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('pt-PT')
}

// Filter functions
const clearFilters = () => {
  selectedCurricularUnit.value = 'all'
  selectedEvaluationType.value = 'all'
  phase1Search.value = ''
}

const updateCurricularUnits = () => {
  const units = new Set<string>()
  revisionRequests.value.forEach(item => {
    if (item.curricularUnitName) {
      units.add(item.curricularUnitName)
    }
  })
  
  curricularUnits.value = [
    { value: 'all', title: 'Todas as Unidades Curriculares' },
    ...Array.from(units).sort().map(unit => ({ value: unit, title: unit }))
  ]
}

const getStatusColor = (status: string) => {
  const colors = {
    'AVAILABLE_FOR_REVISION': 'grey',
    'REVISION_REQUESTED': 'orange',
    'UNDER_TEACHER_REVIEW': 'warning',
    'AWAITING_FINAL_APPROVAL': 'info',
    'APPROVED': 'success',
    'REJECTED': 'red',
  }
  return colors[status] || 'grey'
}

const getStatusText = (status: string) => {
  const texts = {
    'AVAILABLE_FOR_REVISION': 'Disponível para Revisão',
    'REVISION_REQUESTED': 'Revisão Solicitada',
    'UNDER_TEACHER_REVIEW': 'Em Análise Docente',
    'AWAITING_FINAL_APPROVAL': 'Aguardando Aprovação',
    'APPROVED': 'Aprovado',
    'REJECTED': 'Rejeitado',
  }
  return texts[status] || status
}

// Role-based descriptions and summaries
const getRoleDescription = () => {
  if (isStudent.value) {
    return 'Consulte as suas notas e solicite revisões'
  } else if (isTeacher.value) {
    return isMainTeacher.value 
      ? 'Consulte solicitações de revisão e aprove decisões finais'
      : 'Consulte solicitações de revisão de alunos'
  }
  return 'Sistema de revisão de avaliações'
}

const getSummaryColor = () => {
  if (isStudent.value) {
    return 'info'
  }
  const total = getPhaseCount('phase2') + getPhaseCount('phase3')
  return total > 0 ? 'info' : 'success'
}

const getSummaryIconColor = () => {
  if (isStudent.value) {
    return 'info-darken-2'
  }
  const total = getPhaseCount('phase2') + getPhaseCount('phase3')
  return total > 0 ? 'info-darken-2' : 'success-darken-2'
}

const getSummaryIcon = () => {
  if (isStudent.value) {
    return 'mdi-clipboard-account'
  }
  const total = getPhaseCount('phase2') + getPhaseCount('phase3')
  return total > 0 ? 'mdi-clipboard-text-clock' : 'mdi-check-circle'
}

const getSummaryTitle = () => {
  if (isStudent.value) {
    return 'Minhas Avaliações'
  }
  const total = getPhaseCount('phase2') + getPhaseCount('phase3')
  return total > 0 ? 'Revisões Pendentes' : 'Nenhuma Revisão Pendente'
}

const getSummarySubtitle = () => {
  if (isStudent.value) {
    const total = getPhaseCount('phase1')
    return `${total} avaliação${total !== 1 ? 'ões' : ''} registada${total !== 1 ? 's' : ''}.`
  }
  
  const total = getPhaseCount('phase2') + getPhaseCount('phase3')
  if (total === 0) {
    return 'Não há revisões pendentes de avaliação neste momento.'
  }
  
  const phase1Count = getPhaseCount('phase1')
  const phase2Count = getPhaseCount('phase2')
  const phase3Count = getPhaseCount('phase3')
  
  const parts = []
  if (phase1Count > 0) parts.push(`${phase1Count} solicitações`)
  if (phase2Count > 0) parts.push(`${phase2Count} análises docentes`)
  if (phase3Count > 0) parts.push(`${phase3Count} aprovações finais`)
  
  return `${parts.join(', ')} pendentes.`
}

const getPhaseCount = (phase: string) => {
  switch (phase) {
    case 'phase1':
      return phase1Items.value.length
    case 'phase2':
      return phase2Items.value.length
    case 'phase3':
      return phase3Items.value.length
    default:
      return 0
  }
}

// Permission checks
const canRequestRevision = (item: any) => {
  if (!isStudent.value) return false
  
  // Students can request revision if they haven't already requested one
  // or if their previous revision was rejected
  const status = item.status || item.revisionStatus
  return !status || status === 'NONE' || status === 'REJECTED' || status === 'AVAILABLE_FOR_REVISION'
}

const canReviewRevision = (item: any) => {
  return isTeacher.value && item.status === 'REVISION_REQUESTED'
}

const canFinalApprove = (item: any) => {
  return isMainTeacher.value && item.status === 'AWAITING_FINAL_APPROVAL'
}

// Dialog functions
const viewDetails = (item: any) => {
  selectedItem.value = item
  detailsDialog.value = true
}

const openRevisionRequestDialog = (item: any) => {
  selectedItem.value = item
  revisionReason.value = ''
  revisionRequestDialog.value = true
}

const openTeacherReviewDialog = (item: any) => {
  selectedItem.value = item
  suggestedGrade.value = item.currentGrade
  teacherJustification.value = ''
  teacherReviewDialog.value = true
}

const openFinalApprovalDialog = (item: any) => {
  selectedItem.value = item
  finalGrade.value = item.suggestedGrade || item.currentGrade
  finalJustification.value = ''
  finalApprovalDialog.value = true
}

// Action functions
const submitRevisionRequest = async () => {
  if (!revisionRequestForm.value?.validate() || !selectedItem.value) return
  
  submitting.value = true
  try {
    await RemoteService.requestGradeRevision(selectedItem.value.id, revisionReason.value)
    
    // Update local state
    selectedItem.value.status = 'REVISION_REQUESTED'
    selectedItem.value.reason = revisionReason.value
    selectedItem.value.requestedAt = new Date().toISOString()
    
    revisionRequestDialog.value = false
    await fetchRevisionRequests()
  } catch (error) {
    console.error('Error requesting revision:', error)
  } finally {
    submitting.value = false
  }
}

const submitTeacherReview = async () => {
  if (!teacherReviewForm.value?.validate() || !selectedItem.value) return
  
  submitting.value = true
  try {
    await RemoteService.submitTeacherRevision(selectedItem.value.id, {
      suggestedGrade: suggestedGrade.value,
      justification: teacherJustification.value
    })
    
    // Update local state
    selectedItem.value.status = 'AWAITING_FINAL_APPROVAL'
    selectedItem.value.suggestedGrade = suggestedGrade.value
    selectedItem.value.teacherJustification = teacherJustification.value
    selectedItem.value.reviewedAt = new Date().toISOString()
    
    teacherReviewDialog.value = false
    await fetchRevisionRequests()
  } catch (error) {
    console.error('Error submitting teacher review:', error)
  } finally {
    submitting.value = false
  }
}

const approveFinalGrade = async () => {
  if (!finalApprovalForm.value?.validate() || !selectedItem.value) return
  
  submitting.value = true
  try {
    await RemoteService.approveFinalRevision(selectedItem.value.id, {
      finalGrade: finalGrade.value,
      justification: finalJustification.value,
      approved: true
    })
    
    // Update local state
    selectedItem.value.status = 'APPROVED'
    selectedItem.value.finalGrade = finalGrade.value
    selectedItem.value.finalJustification = finalJustification.value
    selectedItem.value.currentGrade = finalGrade.value // Update current grade to final grade
    
    finalApprovalDialog.value = false
    detailsDialog.value = false
    await fetchRevisionRequests()
  } catch (error) {
    console.error('Error approving final revision:', error)
  } finally {
    submitting.value = false
  }
}

const rejectFinalApproval = async () => {
  if (!selectedItem.value) return
  
  submitting.value = true
  try {
    await RemoteService.approveFinalRevision(selectedItem.value.id, {
      finalGrade: null,
      justification: finalJustification.value,
      approved: false
    })
    finalApprovalDialog.value = false
    detailsDialog.value = false
    await fetchRevisionRequests()
  } catch (error) {
    console.error('Error rejecting final revision:', error)
  } finally {
    submitting.value = false
  }
}

// Data fetching
const fetchRevisionRequests = async () => {
  try {
    loading.value = true
    const response = await RemoteService.getRevisionRequests()
    
    if (response && Array.isArray(response)) {
      revisionRequests.value = response
      updateCurricularUnits()
    } else {
      revisionRequests.value = []
      curricularUnits.value = [{ value: 'all', title: 'Todas as Unidades Curriculares' }]
    }
  } catch (error) {
    console.error('Error fetching revision requests:', error)
    revisionRequests.value = []
    curricularUnits.value = [{ value: 'all', title: 'Todas as Unidades Curriculares' }]
  } finally {
    loading.value = false
  }
}

// Initialization
onMounted(async () => {
  await fetchRevisionRequests()
})


</script>

<style scoped>
.max-width-300 {
  max-width: 300px;
}

:deep(.v-card-title) {
  font-weight: bold;
}

:deep(.v-card-subtitle) {
  margin-top: -4px;
}

.bg-blue {
  background-color: #1976D2 !important;
}
</style>
