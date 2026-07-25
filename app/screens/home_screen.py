import flet as ft

def build_home_screen(on_start):
    return ft.Container(
        content=ft.Column(
            [
                ft.Container(
                    content=ft.Text(
                        "Smart Reading App",
                        size=40,
                        weight=ft.FontWeight.BOLD,
                        text_align=ft.TextAlign.CENTER,
                        color=ft.Colors.BLUE_800,
                    ),
                    margin=ft.margin.only(top=50),
                ),
                ft.Container(
                    content=ft.Image(
                        src="images/ui/logo.png",
                        width=250,
                        height=250,
                        fit=ft.ImageFit.CONTAIN,
                    ),
                    expand=True,
                    alignment=ft.alignment.center,
                ),
                ft.Container(
                    content=ft.ElevatedButton(
                        "Start Learning",
                        on_click=on_start,
                        style=ft.ButtonStyle(
                            padding=25,
                            shape=ft.RoundedRectangleBorder(radius=30),
                            bgcolor={"": ft.Colors.ORANGE_500},
                            color={"": ft.Colors.WHITE},
                        ),
                        width=300,
                    ),
                    margin=ft.margin.only(bottom=50),
                ),
            ],
            alignment=ft.MainAxisAlignment.CENTER,
            horizontal_alignment=ft.CrossAxisAlignment.CENTER,
            spacing=20,
        ),
        alignment=ft.alignment.center,
        expand=True,
        padding=20,
    )
