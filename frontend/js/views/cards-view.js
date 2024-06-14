export class CardsView
{
    constructor()
    {

    }

    displayCard(card)
    {
        const cards = document.getElementsByClassName("cards")[0];

        const div_card = document.createElement("div");
        div_card.setAttribute("id", card.idCard);
        div_card.dataset.color = card.color;
        div_card.classList.add("card");
        div_card.innerHTML = card.mot;
        cards.appendChild(div_card);
    }
}

