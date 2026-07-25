import flet as ft
from app.router import AppRouter

async def main(page: ft.Page):
    page.title = "Smart Reading App"
    page.theme_mode = ft.ThemeMode.LIGHT

    # Adaptive safe areas and padding
    page.padding = 0 # We use SafeArea for custom padding
    page.window_center()

    router = AppRouter(page)

    # Wrap main content in SafeArea to handle system insets
    main_container = ft.SafeArea(
        content=ft.Container(expand=True),
        expand=True,
    )
    page.add(main_container)

    # We need to give the router the container to update
    router.page_content = main_container.content

    await router.start()

if __name__ == "__main__":
    ft.app(target=main, assets_dir="assets")
