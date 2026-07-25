import flet as ft
from app.models import Phase
from app.audio_service import AudioService

def build_phonics_tap_screen(phase: Phase, audio_service: AudioService, on_complete):
    items = phase.raw.get("items", [])
    tap_counts = {item["id"]: 0 for item in items}
    required_taps = phase.completion_rule.count or 1

    # Play instruction audio if available
    if phase.instruction_audio:
        # We can't await here easily if this isn't async, but Flet lets us run tasks
        pass

    async def handle_tap(item):
        await audio_service.play(item["audio"])
        tap_counts[item["id"]] += 1

        # Check if all items tapped enough times
        if all(count >= required_taps for count in tap_counts.values()):
            on_complete()

    async def play_instruction(e):
        if phase.instruction_audio:
            await audio_service.play(phase.instruction_audio)

    # UI Components
    grid = ft.GridView(
        expand=1,
        runs_count=3,
        max_extent=150,
        child_aspect_ratio=1.0,
        spacing=10,
        run_spacing=10,
    )

    for item in items:
        grid.controls.append(
            ft.Container(
                content=ft.Text(item["label"], size=40, weight=ft.FontWeight.BOLD),
                alignment=ft.alignment.center,
                bgcolor=ft.Colors.SURFACE_VARIANT,
                border_radius=10,
                on_click=lambda e, i=item: handle_tap(i),
            )
        )

    return ft.Container(
        content=ft.Column(
            [
                ft.Row(
                    [
                        ft.IconButton(ft.Icons.VOLUME_UP, on_click=play_instruction),
                        ft.Text(phase.title, size=24, weight=ft.FontWeight.BOLD, expand=True),
                    ],
                    alignment=ft.MainAxisAlignment.START,
                ),
                ft.Text(phase.instruction_text, size=16, italic=True),
                ft.Divider(),
                grid,
                # Recording Analysis Prompt Integration Placeholder
                ft.Container(
                    content=ft.Row(
                        [
                            ft.Icon(ft.Icons.MIC, color=ft.Colors.BLUE),
                            ft.Text("Tap a letter and try to say it aloud!", size=14),
                        ],
                        alignment=ft.MainAxisAlignment.CENTER,
                    ),
                    padding=10,
                    bgcolor=ft.Colors.BLUE_50,
                    border_radius=5,
                ),
            ],
            expand=True,
            spacing=20,
        ),
        padding=20,
        expand=True,
    )
