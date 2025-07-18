document.addEventListener('DOMContentLoaded', function () {
    let currentStageIndex = 0;
    const totalStages = 5;

    const prevBtn = document.getElementById('prevStageBtn');
    const nextBtn = document.getElementById('nextStageBtn');

    function navigateStage(direction) {
        const newIndex = currentStageIndex + direction;
        if (newIndex < 0 || newIndex >= totalStages) return;

        const currentStage = document.getElementById(`stage-${currentStageIndex}`);
        if (currentStage) {
            currentStage.classList.remove('active');
            currentStage.style.display = 'none';
        }

        currentStageIndex = newIndex;
        const newStage = document.getElementById(`stage-${currentStageIndex}`);
        if (newStage) {
            newStage.classList.add('active');
            newStage.style.display = 'block';
        }

        updateUI();

        const stageContainer = document.getElementById('stageContainer');
        stageContainer.style.opacity = '0.7';
        setTimeout(() => {
            stageContainer.style.opacity = '1';
        }, 150);
    }

    function updateUI() {
        updateStageIndicator();
        updateNavigationButtons();
    }

    function updateStageIndicator() {
        const indicator = document.getElementById('stageIndicator');
        if (indicator) {
            indicator.textContent = `${currentStageIndex + 1} of ${totalStages}`;
        }
    }

    function updateNavigationButtons() {
        if (prevBtn) {
            prevBtn.disabled = currentStageIndex === 0;
            prevBtn.classList.toggle('disabled', currentStageIndex === 0);
        }

        if (nextBtn) {
            nextBtn.disabled = currentStageIndex === totalStages - 1;
            nextBtn.classList.toggle('disabled', currentStageIndex === totalStages - 1);
        }
    }

    function initializeStageDisplay() {
        for (let i = 0; i < totalStages; i++) {
            const stage = document.getElementById(`stage-${i}`);
            if (stage) {
                stage.classList.remove('active');
                stage.style.display = 'none';
            }
        }

        const firstStage = document.getElementById('stage-0');
        if (firstStage) {
            firstStage.classList.add('active');
            firstStage.style.display = 'block';
        }

        currentStageIndex = 0;
    }

    if (prevBtn) prevBtn.addEventListener('click', () => navigateStage(-1));
    if (nextBtn) nextBtn.addEventListener('click', () => navigateStage(1));

    document.addEventListener('keydown', function (e) {
        if (e.key === 'ArrowLeft') navigateStage(-1);
        if (e.key === 'ArrowRight') navigateStage(1);
    });

    initializeStageDisplay();
    updateUI();

    const style = document.createElement('style');
    style.textContent = `
            .stage-board {
                transition: opacity 0.3s ease-in-out;
            }

            #stageContainer {
                transition: opacity 0.2s ease-in-out;
            }

            .btn.disabled {
                opacity: 0.5;
                cursor: not-allowed;
            }
        `;
    document.head.appendChild(style);
});
