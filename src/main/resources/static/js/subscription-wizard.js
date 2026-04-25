document.addEventListener("DOMContentLoaded", () => {
    const root = document.querySelector("[data-subscription-wizard]");
    if (!root) {
        return;
    }

    const messageBox = document.getElementById("wizardMessage");
    const previewCard = document.getElementById("previewCard");
    const nextButton = document.getElementById("nextStepButton");
    const prevButton = document.getElementById("prevStepButton");
    const saveButton = document.getElementById("saveSubscriptionButton");
    const refreshPreviewButton = document.getElementById("refreshPreviewButton");
    const steps = Array.from(root.querySelectorAll(".wizard-step"));
    const stepDots = Array.from(root.querySelectorAll("[data-step-dot]"));

    const state = {
        mode: "create",
        editingSubscriptionId: null,
        step: 1,
        studyLanguage: root.dataset.prefillStudyLanguage,
        explanationLanguage: root.dataset.prefillExplanationLanguage,
        learningStyle: root.dataset.prefillLearningStyle,
        deliveryTime: root.dataset.prefillDeliveryTime,
        isActive: true
    };

    function showMessage(message) {
        messageBox.textContent = message ?? "";
    }

    function clearMessage() {
        showMessage("");
    }

    function updateSelections() {
        root.querySelectorAll(".choice-card[data-field]").forEach((button) => {
            const field = button.dataset.field;
            button.classList.toggle("selected", state[field] === button.dataset.value);
        });
    }

    function updateStep() {
        steps.forEach((step) => {
            step.classList.toggle("active", Number(step.dataset.step) === state.step);
        });
        stepDots.forEach((dot) => {
            dot.classList.toggle("active", Number(dot.dataset.stepDot) <= state.step);
        });

        prevButton.classList.toggle("hidden", state.step === 1);
        nextButton.classList.toggle("hidden", state.step === steps.length);
        saveButton.classList.toggle("hidden", state.step !== steps.length);
        saveButton.textContent = state.mode === "edit" ? "설정 저장" : "구독 저장";

        if (state.step === steps.length) {
            loadPreview();
        }
    }

    function buildPayload() {
        return {
            studyLanguage: state.studyLanguage,
            explanationLanguage: state.explanationLanguage,
            learningStyle: state.learningStyle,
            deliveryTime: state.deliveryTime
        };
    }

    async function requestJson(url, options) {
        const response = await fetch(url, {
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            ...options
        });

        const data = await response.json().catch(() => null);
        if (!response.ok) {
            throw new Error(data?.message || data?.data?.message || "요청 처리 중 오류가 발생했습니다.");
        }

        return data?.data ?? data;
    }

    function renderPreview(preview) {
        document.querySelectorAll("[data-summary]").forEach((element) => {
            const key = element.dataset.summary;
            element.textContent = preview[key] ?? "-";
        });

        const content = preview.previewContent;
        previewCard.textContent = "";
        appendTextElement(previewCard, "p", "eyebrow", content.learningStyleLabel);
        appendTextElement(previewCard, "h3", null, content.title);
        appendTextElement(previewCard, "p", "source-meta", formatSourceMeta(content));
        appendTextElement(previewCard, "p", "feed-source", content.sourceText);
        appendTextElement(previewCard, "p", "eyebrow", "학습 포인트");
        appendTextElement(previewCard, "p", "feed-explanation", content.explanationText);

        const expressions = [content.expressionOne, content.expressionTwo].filter(Boolean);
        if (expressions.length > 0) {
            const pointList = document.createElement("div");
            pointList.className = "point-list compact";
            expressions.forEach((expression) => appendTextElement(pointList, "span", "point-chip", expression));
            previewCard.append(pointList);
        }

        const quizCard = document.createElement("div");
        quizCard.className = "profile-card";
        quizCard.style.marginTop = "16px";
        quizCard.style.padding = "18px";
        appendTextElement(quizCard, "p", "eyebrow", "짧은 복습 문제");
        appendTextElement(quizCard, "p", "feed-explanation", content.quizText ?? "");
        previewCard.append(quizCard);
    }

    function formatSourceMeta(content) {
        const parts = [content.sourceName, content.sourceHost].filter(Boolean);
        return parts.length > 0 ? `출처: ${parts.join(" · ")}` : "";
    }

    function appendTextElement(parent, tagName, className, text) {
        if (!text) {
            return null;
        }
        const element = document.createElement(tagName);
        if (className) {
            element.className = className;
        }
        element.textContent = text;
        parent.append(element);
        return element;
    }

    async function loadPreview() {
        clearMessage();
        renderPreviewMessage("미리보기를 불러오는 중입니다.");

        try {
            const preview = await requestJson(root.dataset.previewApi, {
                method: "POST",
                body: JSON.stringify(buildPayload())
            });
            renderPreview(preview);
        } catch (error) {
            showMessage(error.message);
            renderPreviewMessage("미리보기를 불러오지 못했습니다.");
        }
    }

    function renderPreviewMessage(message) {
        previewCard.textContent = "";
        appendTextElement(previewCard, "p", "helper-text", message);
    }

    function loadSubscriptionToForm(card) {
        state.mode = "edit";
        state.editingSubscriptionId = card.dataset.subscriptionId;
        state.studyLanguage = card.dataset.studyLanguage;
        state.explanationLanguage = card.dataset.explanationLanguage;
        state.learningStyle = card.dataset.learningStyle;
        state.deliveryTime = card.dataset.deliveryTime;
        state.isActive = card.dataset.isActive === "true";
        state.step = location.hash === "#preview" ? steps.length : 1;
        updateSelections();
        updateStep();
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    async function submitSubscription() {
        clearMessage();

        try {
            const payload = buildPayload();
            if (state.mode === "edit") {
                payload.isActive = state.isActive;
            }

            const url = state.mode === "edit"
                ? `${root.dataset.subscriptionsApi}/${state.editingSubscriptionId}`
                : root.dataset.subscriptionsApi;
            const method = state.mode === "edit" ? "PATCH" : "POST";

            const result = await requestJson(url, {
                method,
                body: JSON.stringify(payload)
            });

            window.location.href = `${root.dataset.completedPrefix}${result.subscriptionId}`;
        } catch (error) {
            showMessage(error.message);
        }
    }

    root.addEventListener("click", (event) => {
        const button = event.target.closest(".choice-card[data-field]");
        if (button) {
            clearMessage();
            state[button.dataset.field] = button.dataset.value;
            updateSelections();
        }

        const editButton = event.target.closest(".edit-subscription-button");
        if (editButton) {
            const card = editButton.closest(".subscription-card");
            if (card) {
                loadSubscriptionToForm(card);
            }
        }

        const toggleButton = event.target.closest(".toggle-subscription-button");
        if (toggleButton) {
            const card = toggleButton.closest(".subscription-card");
            if (!card) {
                return;
            }

            const nextActive = card.dataset.isActive !== "true";
            requestJson(`${root.dataset.subscriptionsApi}/${card.dataset.subscriptionId}`, {
                method: "PATCH",
                body: JSON.stringify({ isActive: nextActive })
            }).then(() => window.location.reload())
              .catch((error) => showMessage(error.message));
        }

        const deleteButton = event.target.closest(".delete-subscription-button");
        if (deleteButton) {
            const card = deleteButton.closest(".subscription-card");
            if (!card) {
                return;
            }

            if (!window.confirm("이 학습 구독을 삭제하시겠습니까?")) {
                return;
            }

            requestJson(`${root.dataset.subscriptionsApi}/${card.dataset.subscriptionId}`, {
                method: "DELETE"
            }).then(() => window.location.reload())
              .catch((error) => showMessage(error.message));
        }
    });

    nextButton.addEventListener("click", () => {
        if (state.step < steps.length) {
            state.step += 1;
            updateStep();
        }
    });

    prevButton.addEventListener("click", () => {
        if (state.step > 1) {
            state.step -= 1;
            updateStep();
        }
    });

    saveButton.addEventListener("click", submitSubscription);
    refreshPreviewButton.addEventListener("click", loadPreview);

    updateSelections();
    updateStep();

    const editSubscriptionId = root.dataset.editSubscriptionId;
    if (editSubscriptionId) {
        const targetCard = document.querySelector(`.subscription-card[data-subscription-id="${editSubscriptionId}"]`);
        if (targetCard) {
            loadSubscriptionToForm(targetCard);
        }
    }
});
