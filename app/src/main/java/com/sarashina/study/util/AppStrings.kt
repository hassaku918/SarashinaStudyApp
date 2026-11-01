package com.sarashina.study.util

enum class AppLang(val code: String, val label: String) {
    JA("ja", "日本語"),
    EN("en", "English"),
    KO("ko", "한국어"),
    ZH("zh", "中文"),
    RU("ru", "Русский"),
    DE("de", "Deutsch"),
    FR("fr", "Français"),
    FA("fa", "فارسی"),
    NL("nl", "Nederlands");

    companion object {
        fun fromCode(c: String?) = entries.find { it.code == c } ?: JA
    }
}

data class UiStrings(
    val appTitle: String,
    val appSubtitle: String,
    val askPlaceholder: String,
    val settings: String,
    val clearHistory: String,
    val clearConfirmTitle: String,
    val clearConfirmBody: String,
    val delete: String,
    val cancel: String,
    val save: String,
    val responding: String,
    val respondingHint: String,
    val emptyTitle: String,
    val emptyBody: String,
    val notes: String,
    val notesHint: String,
    val newNote: String,
    val noteTitle: String,
    val noteBody: String,
    val webSearch: String,
    val webSearchOn: String,
    val chat: String,
    val theme: String,
    val fontSize: String,
    val apiKey: String,
    val reasoning: String,
    val systemPrompt: String,
    val appLock: String,
    val language: String,
    val scanImage: String,
    val scanPdf: String
)

object AppStrings {
    fun of(lang: AppLang): UiStrings = when (lang) {
        AppLang.JA -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "受験勉強AI · 推論モデル",
            askPlaceholder = "質問を入力…", settings = "設定", clearHistory = "履歴クリア",
            clearConfirmTitle = "履歴を削除", clearConfirmBody = "会話履歴をすべて削除します。",
            delete = "削除", cancel = "キャンセル", save = "保存",
            responding = "応答中…", respondingHint = "画面を閉じてもバックグラウンドで続きます",
            emptyTitle = "問題をスキャンするか質問してみよう",
            emptyBody = "画像・PDFのOCR → 推論モデルが解説\n学習メモとWeb検索も利用できます",
            notes = "学習メモ", notesHint = "有効なメモは毎回AIが参照します",
            newNote = "新規メモ", noteTitle = "タイトル", noteBody = "内容（目標・弱点・解き方の指示など）",
            webSearch = "Web検索", webSearchOn = "検索して回答", chat = "チャット",
            theme = "テーマ", fontSize = "文字サイズ", apiKey = "APIキー",
            reasoning = "推論の深さ", systemPrompt = "システムプロンプト", appLock = "アプリロック",
            language = "UI言語", scanImage = "画像スキャン", scanPdf = "PDF"
        )
        AppLang.EN -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "Exam tutor · reasoning model",
            askPlaceholder = "Ask a question…", settings = "Settings", clearHistory = "Clear history",
            clearConfirmTitle = "Delete history", clearConfirmBody = "This will permanently delete all chat messages.",
            delete = "Delete", cancel = "Cancel", save = "Save",
            responding = "Responding…", respondingHint = "Continues in background if you leave",
            emptyTitle = "Scan a problem or ask anything",
            emptyBody = "OCR → AI explanation\nStudy notes & web search available",
            notes = "Study notes", notesHint = "Enabled notes are sent to the AI each time",
            newNote = "New note", noteTitle = "Title", noteBody = "Content (goals, weak points…)",
            webSearch = "Web search", webSearchOn = "Search & answer", chat = "Chat",
            theme = "Theme", fontSize = "Font size", apiKey = "API key",
            reasoning = "Reasoning depth", systemPrompt = "System prompt", appLock = "App lock",
            language = "UI language", scanImage = "Scan image", scanPdf = "PDF"
        )
        AppLang.KO -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "수험 공부 AI",
            askPlaceholder = "질문을 입력…", settings = "설정", clearHistory = "기록 삭제",
            clearConfirmTitle = "기록 삭제", clearConfirmBody = "모든 대화 기록을 삭제합니다.",
            delete = "삭제", cancel = "취소", save = "저장",
            responding = "응답 중…", respondingHint = "백그라운드에서 계속됩니다",
            emptyTitle = "문제를 스캔하거나 질문해 보세요", emptyBody = "OCR → AI 해설",
            notes = "학습 메모", notesHint = "활성화된 메모는 AI가 참조합니다",
            newNote = "새 메모", noteTitle = "제목", noteBody = "내용",
            webSearch = "웹 검색", webSearchOn = "검색 후 답변", chat = "채팅",
            theme = "테마", fontSize = "글자 크기", apiKey = "API 키",
            reasoning = "추론 깊이", systemPrompt = "시스템 프롬프트", appLock = "앱 잠금",
            language = "UI 언어", scanImage = "이미지 스캔", scanPdf = "PDF"
        )
        AppLang.ZH -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "备考 AI",
            askPlaceholder = "输入问题…", settings = "设置", clearHistory = "清除记录",
            clearConfirmTitle = "删除记录", clearConfirmBody = "将删除全部对话记录。",
            delete = "删除", cancel = "取消", save = "保存",
            responding = "回答中…", respondingHint = "后台继续",
            emptyTitle = "扫描题目或直接提问", emptyBody = "OCR → AI 讲解",
            notes = "学习笔记", notesHint = "启用的笔记会提供给 AI",
            newNote = "新建笔记", noteTitle = "标题", noteBody = "内容",
            webSearch = "网络搜索", webSearchOn = "搜索并回答", chat = "聊天",
            theme = "主题", fontSize = "字体大小", apiKey = "API 密钥",
            reasoning = "推理深度", systemPrompt = "系统提示", appLock = "应用锁",
            language = "界面语言", scanImage = "扫描图片", scanPdf = "PDF"
        )
        AppLang.RU -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "Репетитор",
            askPlaceholder = "Задайте вопрос…", settings = "Настройки", clearHistory = "Очистить",
            clearConfirmTitle = "Удалить историю", clearConfirmBody = "Все сообщения будут удалены.",
            delete = "Удалить", cancel = "Отмена", save = "Сохранить",
            responding = "Ответ…", respondingHint = "Продолжается в фоне",
            emptyTitle = "Сканируйте задачу", emptyBody = "OCR → объяснение ИИ",
            notes = "Заметки", notesHint = "Включённые заметки идут в контекст",
            newNote = "Новая", noteTitle = "Заголовок", noteBody = "Текст",
            webSearch = "Поиск", webSearchOn = "Искать и ответить", chat = "Чат",
            theme = "Тема", fontSize = "Шрифт", apiKey = "API-ключ",
            reasoning = "Глубина", systemPrompt = "Системный промпт", appLock = "Блокировка",
            language = "Язык UI", scanImage = "Скан", scanPdf = "PDF"
        )
        AppLang.DE -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "Prüfungs-KI",
            askPlaceholder = "Frage eingeben…", settings = "Einstellungen", clearHistory = "Verlauf löschen",
            clearConfirmTitle = "Verlauf löschen", clearConfirmBody = "Alle Nachrichten werden gelöscht.",
            delete = "Löschen", cancel = "Abbrechen", save = "Speichern",
            responding = "Antwortet…", respondingHint = "Läuft im Hintergrund weiter",
            emptyTitle = "Aufgabe scannen", emptyBody = "OCR → KI-Erklärung",
            notes = "Lernnotizen", notesHint = "Aktive Notizen gehen an die KI",
            newNote = "Neu", noteTitle = "Titel", noteBody = "Inhalt",
            webSearch = "Websuche", webSearchOn = "Suchen & antworten", chat = "Chat",
            theme = "Design", fontSize = "Schriftgröße", apiKey = "API-Schlüssel",
            reasoning = "Reasoning", systemPrompt = "Systemprompt", appLock = "App-Sperre",
            language = "UI-Sprache", scanImage = "Bild", scanPdf = "PDF"
        )
        AppLang.FR -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "Tuteur exam",
            askPlaceholder = "Posez une question…", settings = "Réglages", clearHistory = "Effacer",
            clearConfirmTitle = "Supprimer", clearConfirmBody = "Tout l'historique sera effacé.",
            delete = "Supprimer", cancel = "Annuler", save = "Enregistrer",
            responding = "Réponse…", respondingHint = "Continue en arrière-plan",
            emptyTitle = "Scannez un exercice", emptyBody = "OCR → explication IA",
            notes = "Notes d'étude", notesHint = "Les notes actives sont envoyées à l'IA",
            newNote = "Nouvelle", noteTitle = "Titre", noteBody = "Contenu",
            webSearch = "Recherche web", webSearchOn = "Chercher et répondre", chat = "Chat",
            theme = "Thème", fontSize = "Taille", apiKey = "Clé API",
            reasoning = "Profondeur", systemPrompt = "Prompt système", appLock = "Verrouillage",
            language = "Langue UI", scanImage = "Image", scanPdf = "PDF"
        )
        AppLang.FA -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "معلم آزمون",
            askPlaceholder = "سوال بپرسید…", settings = "تنظیمات", clearHistory = "پاک کردن",
            clearConfirmTitle = "حذف تاریخچه", clearConfirmBody = "همه پیام‌ها حذف می‌شوند.",
            delete = "حذف", cancel = "لغو", save = "ذخیره",
            responding = "در حال پاسخ…", respondingHint = "در پس‌زمینه ادامه می‌یابد",
            emptyTitle = "مسئله را اسکن کنید", emptyBody = "OCR → توضیح",
            notes = "یادداشت مطالعه", notesHint = "یادداشت‌های فعال به AI ارسال می‌شوند",
            newNote = "جدید", noteTitle = "عنوان", noteBody = "متن",
            webSearch = "جستجوی وب", webSearchOn = "جستجو و پاسخ", chat = "گفتگو",
            theme = "پوسته", fontSize = "اندازه قلم", apiKey = "کلید API",
            reasoning = "عمق استدلال", systemPrompt = "پرامپت سیستم", appLock = "قفل برنامه",
            language = "زبان رابط", scanImage = "اسکن تصویر", scanPdf = "PDF"
        )
        AppLang.NL -> UiStrings(
            appTitle = "Sarashina Study", appSubtitle = "Examentutor",
            askPlaceholder = "Stel een vraag…", settings = "Instellingen", clearHistory = "Geschiedenis wissen",
            clearConfirmTitle = "Geschiedenis wissen", clearConfirmBody = "Alle berichten worden verwijderd.",
            delete = "Verwijderen", cancel = "Annuleren", save = "Opslaan",
            responding = "Bezig…", respondingHint = "Gaat door op de achtergrond",
            emptyTitle = "Scan een opgave", emptyBody = "OCR → AI-uitleg",
            notes = "Studienotities", notesHint = "Actieve notities gaan naar de AI",
            newNote = "Nieuw", noteTitle = "Titel", noteBody = "Inhoud",
            webSearch = "Webzoeken", webSearchOn = "Zoeken & antwoorden", chat = "Chat",
            theme = "Thema", fontSize = "Lettergrootte", apiKey = "API-sleutel",
            reasoning = "Redeneerdiepte", systemPrompt = "Systeemprompt", appLock = "App-vergrendeling",
            language = "UI-taal", scanImage = "Afbeelding", scanPdf = "PDF"
        )
    }
}
