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
        difficultyLevel: root.dataset.prefillDifficultyLevel,
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
        nextButton.classList.toggle("hidden", state.step === 6);
        saveButton.classList.toggle("hidden", state.step !== 6);
        saveButton.textContent = state.mode === "edit" ? "설정 저장" : "구독 저장";

        if (state.step === 6) {
            loadPreview();
        }
    }

    function buildPayload() {
        return {
            studyLanguage: state.studyLanguage,
            explanationLanguage: state.explanationLanguage,
            learningStyle: state.learningStyle,
            difficultyLevel: state.difficultyLevel,
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
        previewCard.innerHTML = `
            <p class="eyebrow">${content.learningStyleLabel}</p>
            <h3>${content.title}</h3>
            <p class="feed-summary">${content.summary}</p>
            <p class="feed-source">${content.sourceText}</p>
            <p class="feed-explanation">${content.explanationText}</p>
            <div class="point-list compact">
                <span class="point-chip">${content.expressionOne ?? ""}</span>
                <span class="point-chip">${content.expressionTwo ?? ""}</span>
            </div>
            <div class="profile-card" style="margin-top:16px;padding:18px;">
                <p class="eyebrow">짧은 복습 문제</p>
                <p class="feed-explanation">${content.quizText ?? ""}</p>
            </div>
        `;
    }

    async function loadPreview() {
        clearMessage();
        previewCard.innerHTML = `<p class="helper-text">미리보기를 불러오는 중입니다.</p>`;

        try {
            const preview = await requestJson(root.dataset.previewApi, {
                method: "POST",
                body: JSON.stringify(buildPayload())
            });
            renderPreview(preview);
        } catch (error) {
            showMessage(error.message);
            previewCard.innerHTML = `<p class="helper-text">미리보기를 불러오지 못했습니다.</p>`;
        }
    }

    function loadSubscriptionToForm(card) {
        state.mode = "edit";
        state.editingSubscriptionId = card.dataset.subscriptionId;
        state.studyLanguage = card.dataset.studyLanguage;
        state.explanationLanguage = card.dataset.explanationLanguage;
        state.learningStyle = card.dataset.learningStyle;
        state.difficultyLevel = card.dataset.difficultyLevel;
        state.deliveryTime = card.dataset.deliveryTime;
        state.isActive = card.dataset.isActive === "true";
        state.step = location.hash === "#preview" ? 6 : 1;
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
        if (state.step < 6) {
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
