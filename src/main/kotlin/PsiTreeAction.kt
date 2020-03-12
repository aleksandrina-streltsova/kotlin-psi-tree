import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement


class PsiTreeAction : AnAction() {
    private var indent = 0
    private var selectionStart = 0;
    private var selectionEnd = 0;
    private val psiTreeText = StringBuilder("")

    private fun printPsiElement(psiElement: PsiElement) {
        if (psiElement.textOffset in selectionStart until selectionEnd) {
            psiTreeText.append("\t".repeat(indent))
            psiTreeText.append("$psiElement\n")
        }
        indent++
        psiElement.children.map { printPsiElement(it) }
        indent--
    }

    private fun showResult(e: AnActionEvent) {
        val toolWindowManager = ToolWindowManager.getInstance(e.project!!)
        val toolWindow = toolWindowManager.getToolWindow("PsiTree") ?: toolWindowManager.registerToolWindow(
            "PsiTree",
            true,
            ToolWindowAnchor.RIGHT
        )
        val consoleView: ConsoleView =
            TextConsoleBuilderFactory.getInstance().createBuilder(e.project!!).console
        val content = toolWindow.contentManager.factory
            .createContent(consoleView.component, " Output", false)
        toolWindow.contentManager.addContent(content)
        consoleView.print(psiTreeText.toString(), ConsoleViewContentType.NORMAL_OUTPUT);
    }

    override fun update(e: AnActionEvent) {
        // Using the event, evaluate the context, and enable or disable the action.
        val project = e.project
        e.presentation.isEnabledAndVisible = project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        psiTreeText.clear()
        val editor = e.getData(CommonDataKeys.EDITOR)
        selectionStart = editor?.selectionModel?.selectionStart ?: 0
        selectionEnd = editor?.selectionModel?.selectionEnd ?: 0
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        psiFile?.let {
            it.children.map { child -> printPsiElement(child) }
        }
        showResult(e)
    }
}
