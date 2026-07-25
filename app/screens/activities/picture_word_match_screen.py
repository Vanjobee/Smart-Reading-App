import flet as ft
import random
from app.models import Phase
from app.audio_service import AudioService

def build_picture_word_match_screen(phase: Phase, audio_service: AudioService, on_complete):
    items = phase.raw.get("items", [])
    current_item_index = 0

    image_display = ft.Image(width=300, height=300, fit=ft.ImageFit.CONTAIN)
    options_row = ft.ResponsiveRow(spacing=10, run_spacing=10)

    def load_item():
        nonlocal current_item_index
        if current_item_index >= len(items):
            on_complete()
            return

        item = items[current_item_index]
        image_display.src = item["image"]

        # Create options (correct one + random others)
        correct_word = item["word"]
        all_words = [i["word"] for i in items]
        wrong_options = random.sample([w for w in all_words if w != correct_word], min(3, len(all_words)-1))
        options = wrong_options + [correct_word]
        random.shuffle(options)

        options_row.controls.clear()
        for opt in options:
            options_row.controls.append(
                ft.Container(
                    content=ft.ElevatedButton(
                        opt.upper(),
                        on_click=lambda e, o=opt: check_answer(o),
                        style=ft.ButtonStyle(shape=ft.RoundedRectangleBorder(radius=10)),
                        expand=True,
                    ),
                    col={"sm": 6, "md": 3},
                )
            )

    async def check_answer(selected):
        nonlocal current_item_index
        item = items[current_item_index]
        if selected == item["word"]:
            await audio_service.play("audio/praise/correct.mp3") # Assuming some praise sounds
            current_item_index += 1
            load_item()
            image_display.update()
            options_row.update()
        else:
            await audio_service.play("audio/praise/try_again.mp3")

    load_item()

    return ft.Container(
        content=ft.Column(
            [
                ft.Text(phase.title, size=24, weight=ft.FontWeight.BOLD),
                ft.Text(phase.instruction_text, size=16),
                ft.Divider(),
                ft.Container(image_display, alignment=ft.alignment.center, expand=True),
                options_row,
            ],
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=20,
            expand=True,
        ),
        padding=20,
        expand=True,
    )
