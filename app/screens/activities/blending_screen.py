import flet as ft
import random
import asyncio
from app.models import Phase
from app.audio_service import AudioService

def build_blending_screen(phase: Phase, audio_service: AudioService, on_complete):
    word_pool = phase.raw.get("random_word_pool", [])
    words_to_complete = phase.completion_rule.count or 5
    completed_count = 0
    current_word = random.choice(word_pool) if word_pool else ""

    word_display = ft.Row(alignment=ft.MainAxisAlignment.CENTER, spacing=10)

    def update_word():
        nonlocal current_word
        if word_pool:
            current_word = random.choice(word_pool)
        word_display.controls.clear()
        for char in current_word:
            word_display.controls.append(
                ft.Container(
                    content=ft.Text(char.upper(), size=50, weight=ft.FontWeight.BOLD),
                    padding=10,
                    bgcolor=ft.Colors.AMBER_100,
                    border_radius=5,
                )
            )

    async def blend_word(e):
        nonlocal completed_count
        # Play each letter sound
        for char in current_word:
            # Assuming phonics audio follows a naming convention if not explicitly in pool
            audio_path = f"audio/phonics/{char.lower()}.mp3"
            await audio_service.play(audio_path)
            await asyncio.sleep(0.5)

        # Play full word sound
        await audio_service.play(f"audio/words/{current_word.lower()}.mp3")

        completed_count += 1
        if completed_count >= words_to_complete:
            on_complete()
        else:
            update_word()
            word_display.update()

    update_word()

    return ft.Container(
        content=ft.Column(
            [
                ft.Text(phase.title, size=24, weight=ft.FontWeight.BOLD),
                ft.Text(phase.instruction_text, size=16),
                ft.Divider(),
                ft.Container(
                    content=word_display,
                    padding=40,
                    alignment=ft.alignment.center,
                    expand=True,
                ),
                ft.ElevatedButton(
                    "Blend and Read",
                    icon=ft.Icons.PLAY_ARROW,
                    on_click=blend_word,
                    style=ft.ButtonStyle(padding=20),
                ),
                # Recording Analysis Prompt Integration
                ft.TextButton(
                    icon=ft.Icons.MIC,
                    text="Record your pronunciation",
                    on_click=lambda _: print("Recording for analysis..."),
                )
            ],
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=20,
            expand=True,
        ),
        padding=20,
        expand=True,
    )
