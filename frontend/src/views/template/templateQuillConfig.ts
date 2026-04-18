/**
 * Shared Quill toolbar + options for template create/edit pages.
 * Use a single `options` object (instead of separate `toolbar` prop) so `history` and other modules merge correctly with @vueup/vue-quill.
 */
export const templateQuillToolbar = [
  [{ header: [1, 2, 3, 4, 5, 6, false] }],
  [{ font: [] }],
  ['bold', 'italic', 'underline', 'strike', { color: [] }, { background: [] }],
  [{ script: 'sub' }, { script: 'super' }],
  [{ list: 'ordered' }, { list: 'bullet' }, { list: 'check' }],
  [{ indent: '-1' }, { indent: '+1' }],
  [{ direction: 'rtl' }],
  [{ align: [] }],
  ['link', 'image', 'video'],
  ['blockquote', 'code-block', { header: 1 }, { header: 2 }],
  ['clean']
] as const

export const templateQuillEditorOptions = {
  modules: {
    toolbar: templateQuillToolbar,
    history: {
      delay: 800,
      maxStack: 200
    }
  }
}
