import flet as ft
from app.models import Phase
from app.audio_service import AudioService

def build_sentence_pyramid_screen(phase: Phase, audio_service: AudioService, on_complete):
    # Example pyramid data (this could be in JSON)
    pyramid_lines = [
        "A",
        "A pin",
        "A pin sat"
    ]

    current_line = 0
    pyramid_display = ft.Column(horizontal_alignment=ft.CrossAxisAlignment.CENTER, spacing=10)

    def load_pyramid():
        pyramid_display.controls.clear()
        for i in range(current_line + 1):
            pyramid_display.controls.append(
                ft.Text(pyramid_lines[i], size=30, weight=ft.FontWeight.BOLD)
            )

    async def next_step(e):
        nonlocal current_line
        current_line += 1
        if current_line >= len(pyramid_lines):
            on_complete()
        else:
            load_pyramid()
            pyramid_display.update()

    load_pyramid()

    return ft.Container(
        content=ft.Column(
            [
                ft.Text(phase.title, size=24, weight=ft.FontWeight.BOLD),
                ft.Text(phase.instruction_text, size=16),
                ft.Divider(),
                ft.Container(
                    content=pyramid_display,
                    alignment=ft.alignment.center,
                    expand=True,
                ),
                ft.ElevatedButton("Next", on_click=next_step, style=ft.ButtonStyle(padding=20)),
                # Recording Analysis
                ft.IconButton(ft.Icons.MIC, on_click=lambda _: print("Recording sentence..."), icon_size=40),
            ],
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=20,
            expand=True,
        ),
        padding=20,
        expand=True,
    )
