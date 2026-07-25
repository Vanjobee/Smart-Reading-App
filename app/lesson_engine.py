from app.models import Level, Phase


class LessonEngine:
    def __init__(self, level: Level):
        self.level = level

    def get_first_phase(self) -> Phase:
        return self.level.phases[0]

    def get_phase(self, phase_id: str) -> Phase:
        for phase in self.level.phases:
            if phase.id == phase_id:
                return phase
        return self.get_first_phase()

    def get_next_phase(self, current_phase_id: str) -> Phase | None:
        for index, phase in enumerate(self.level.phases):
            if phase.id == current_phase_id:
                next_index = index + 1
                if next_index < len(self.level.phases):
                    return self.level.phases[next_index]
        return None