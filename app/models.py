from dataclasses import dataclass, field
from typing import Any


@dataclass(frozen=True)
class CompletionRule:
    type: str
    count: int | None = None
    blend_delay_seconds: int | None = None


@dataclass(frozen=True)
class Phase:
    id: str
    type: str
    title: str
    instruction_text: str
    instruction_audio: str | None
    completion_rule: CompletionRule
    raw: dict[str, Any] = field(default_factory=dict)


@dataclass(frozen=True)
class Level:
    level_id: str
    title: str
    theme: str
    phases: list[Phase]


@dataclass
class ProgressState:
    current_level_id: str = "level_1"
    current_phase_id: str = "l1_phase_1_phonics"
    completed_phases: list[str] = field(default_factory=list)
    completed_levels: list[str] = field(default_factory=list)