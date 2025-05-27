document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("searchBtn").addEventListener("click", function () {
        const date = document.getElementById("date").value;
        const facility = document.getElementById("facility").value;
        const startTime = document.getElementById("startTime").value;
        const endTime = document.getElementById("endTime").value;

        fetch("/api/classrooms/search", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ date, facility, startTime, endTime })
        })
        .then(response => response.json())
        .then(data => {
            const tbody = document.querySelector("#resultTable tbody");
            tbody.innerHTML = "";

            data.forEach(room => {
                const tr = document.createElement("tr");
                tr.innerHTML = `
                    <td>${room.building}</td>
                    <td>${room.floor}</td>
                    <td>${room.name}</td>
                    <td>${room.capacity}</td>
                    <td>${room.availableTime}</td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(error => {
            console.error("검색 실패:", error);
            alert("검색 중 오류가 발생했습니다.");
        });
    });
});