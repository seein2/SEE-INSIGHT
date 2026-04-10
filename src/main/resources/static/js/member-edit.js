(() => {
    const root = document.querySelector("[data-member-edit]");
    if (!root) {
        return;
    }

    const form = document.getElementById("memberEditForm");
    const message = document.getElementById("memberEditMessage");
    const submitButton = document.getElementById("memberEditSubmitButton");
    const updateApi = root.dataset.updateApi;
    const redirectUrl = root.dataset.redirectUrl;

    function setMessage(text, isError = true) {
        message.textContent = text || "";
        message.classList.toggle("is-success", !isError && Boolean(text));
        message.classList.toggle("is-error", isError && Boolean(text));
    }

    async function submitMemberEdit(event) {
        event.preventDefault();
        setMessage("");

        const formData = new FormData(form);
        const nickname = String(formData.get("nickname") || "").trim();

        if (!nickname || nickname.length < 2 || nickname.length > 20) {
            setMessage("닉네임은 2~20자 이내로 입력해주세요.");
            return;
        }

        submitButton.disabled = true;
        submitButton.textContent = "저장 중...";

        try {
            const response = await fetch(updateApi, {
                method: "PATCH",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({ nickname })
            });

            if (!response.ok) {
                const errorBody = await response.json().catch(() => null);
                setMessage(errorBody?.message || "회원정보 수정에 실패했습니다.");
                return;
            }

            window.location.href = redirectUrl;
        } catch (error) {
            setMessage("네트워크 오류로 회원정보 수정에 실패했습니다.");
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = "저장";
        }
    }

    form.addEventListener("submit", submitMemberEdit);
})();
