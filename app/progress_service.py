import json
import flet as ft

from app.constants import APP_PREF_PREFIX
from app.models import ProgressState

PROGRESS_KEY = APP_PREF_PREFIX + "progress"


class ProgressService:
    def __init__(self):
        self._prefs = ft.SharedPreferences()

    async def load_progress(self) -> ProgressState:
        raw = await self._prefs.get(PROGRESS_KEY)
        if not raw:
            return ProgressState()

        data = json.loads(raw)
        return ProgressState(
            current_level_id=data.get("current_level_id", "level_1"),
            current_phase_id=data.get("current_phase_id", "l1_phase_1_phonics"),
            completed_phases=data.get("completed_phases", []),
            completed_levels=data.get("completed_levels", []),
        )

    async def save_progress(self, state: ProgressState) -> None:
        await self._prefs.set(PROGRESS_KEY, json.dumps(state.__dict__))

    async def mark_phase_complete(self, state: ProgressState, level_id: str, phase_id: str) -> ProgressState:
        key = f"{level_id}:{phase_id}"
        if key not in state.completed_phases:
            state.completed_phases.append(key)
        state.current_level_id = level_id
        state.current_phase_id = phase_id
        await self.save_progress(state)
        return state

    async def reset_progress(self) -> None:
        keys = await self._prefs.get_keys(APP_PREF_PREFIX)
        for key in keys:
            await self._prefs.remove(key)