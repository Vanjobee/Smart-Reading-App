import flet as ft

from app.audio_service import AudioService
from app.content_loader import ContentLoader
from app.lesson_engine import LessonEngine
from app.progress_service import ProgressService
from app.screens.home_screen import build_home_screen
from app.screens.activities.phonics_tap_screen import build_phonics_tap_screen
from app.screens.activities.blending_screen import build_blending_screen
from app.screens.activities.picture_word_match_screen import build_picture_word_match_screen
from app.screens.activities.sentence_pyramid_screen import build_sentence_pyramid_screen


class AppRouter:
    def __init__(self, page: ft.Page):
        self.page = page
        self.loader = ContentLoader()
        self.level = self.loader.load_level("data/level_1.json")
        self.engine = LessonEngine(self.level)
        self.progress_service = ProgressService()
        self.audio_service = AudioService(page)
        self.progress = None

    async def start(self) -> None:
        self.progress = await self.progress_service.load_progress()
        self.show_home()

    def clear(self) -> None:
        self.page.controls.clear()

    def show_home(self) -> None:
        self.clear()
        screen = build_home_screen(on_start=lambda _: self.show_current_phase())
        self.page.add(ft.SafeArea(screen, expand=True))
        self.page.update()

    def show_current_phase(self) -> None:
        phase = self.engine.get_phase(self.progress.current_phase_id)
        self.show_phase(phase)

    def show_phase(self, phase) -> None:
        self.clear()
        on_complete = lambda: self.page.run_task(self.complete_phase, phase.id)

        if phase.type == "phonics_tap":
            content = build_phonics_tap_screen(phase, self.audio_service, on_complete)
        elif phase.type == "blending":
            content = build_blending_screen(phase, self.audio_service, on_complete)
        elif phase.type == "picture_word_match":
            content = build_picture_word_match_screen(phase, self.audio_service, on_complete)
        elif phase.type == "sentence_pyramid":
            content = build_sentence_pyramid_screen(phase, self.audio_service, on_complete)
        else:
            content = ft.Text(f"Unsupported phase type: {phase.type}")

        # Use ScrollView for content-heavy screens to prevent clipping
        scrollable_content = ft.Column([content], scroll=ft.ScrollMode.AUTO, expand=True)
        self.page.add(ft.SafeArea(scrollable_content, expand=True))
        self.page.update()

    async def complete_phase(self, phase_id: str) -> None:
        next_phase = self.engine.get_next_phase(phase_id)
        await self.progress_service.mark_phase_complete(self.progress, self.level.level_id, phase_id)

        self.clear()