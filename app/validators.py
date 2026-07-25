from pathlib import Path
from app.models import Level, Phase

SUPPORTED_ACTIVITY_TYPES = {
    "phonics_tap",
    "blending",
    "picture_word_match",
    "sentence_pyramid",
}


def validate_level(level: Level, assets_dir: Path) -> list[str]:
    errors: list[str] = []
    phase_ids: set[str] = set()

    for phase in level.phases:
        if phase.id in phase_ids:
            errors.append(f"Duplicate phase id: {phase.id}")
        phase_ids.add(phase.id)

        if phase.type not in SUPPORTED_ACTIVITY_TYPES:
            errors.append(f"Unsupported activity type: {phase.type}")

        errors.extend(validate_phase_assets(phase, assets_dir))

    return errors


def validate_phase_assets(phase: Phase, assets_dir: Path) -> list[str]:
    errors: list[str] = []

    def check_asset(path: str | None, label: str) -> None:
        if path and not (assets_dir / path).exists():
            errors.append(f"Missing {label}: {path} in phase {phase.id}")

    check_asset(phase.instruction_audio, "instruction audio")

    raw = phase.raw
    for item in raw.get("items", []):
        check_asset(item.get("audio"), "item audio")
        check_asset(item.get("word_audio"), "word audio")
        check_asset(item.get("image"), "image")

    for sentence in raw.get("sentences", []):
        check_asset(sentence.get("sentence_audio"), "sentence audio")

    return errors