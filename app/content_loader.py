import json
from pathlib import Path
from typing import Any

from app.constants import get_assets_dir
from app.models import CompletionRule, Level, Phase


class ContentLoader:
    def __init__(self, assets_dir: Path | None = None):
        self.assets_dir = assets_dir or get_assets_dir()

    def load_json(self, relative_path: str) -> dict[str, Any]:
        path = self.assets_dir / relative_path
        with path.open("r", encoding="utf-8") as file:
            return json.load(file)

    def load_level(self, relative_path: str) -> Level:
        raw_level = self.load_json(relative_path)
        phases: list[Phase] = []

        for raw_phase in raw_level["phases"]:
            raw_rule = raw_phase["completion_rule"]
            rule = CompletionRule(
                type=raw_rule["type"],
                count=raw_rule.get("count"),
                blend_delay_seconds=raw_rule.get("blend_delay_seconds"),
            )
            phases.append(
                Phase(
                    id=raw_phase["id"],
                    type=raw_phase["type"],
                    title=raw_phase["title"],
                    instruction_text=raw_phase.get("instruction_text", ""),
                    instruction_audio=raw_phase.get("instruction_audio"),
                    completion_rule=rule,
                    raw=raw_phase,
                )
            )

        return Level(
            level_id=raw_level["level_id"],
            title=raw_level["title"],
            theme=raw_level.get("theme", "farm"),
            phases=phases,
        )