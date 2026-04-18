/** 模板表单页：AI 分析结果（与后端 TemplateAiAssistantService 对齐） */

export interface AiTemplateAnalysisResult {
  summary: string
  score: number
  risks: Array<{ level?: string; content?: string; anchor?: string }>
  suggestions: string[]
  keyInfo: {
    templateName?: string
    category?: string
    structureNotes?: string
    variableHints?: string
    consistencyWarnings?: string[]
  }
}
