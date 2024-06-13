export class CardsView
{
    constructor()
    {
        this.#displayCards()
    }

    #displayCards(product)
    {
        const cards = document.getElementsByClassName("cards")[0];

        for(let i = 0; i < 25; i++)
        {
            const card = document.createElement("div");
            card.classList.add("card");
            card.innerHTML = "TEST";
            cards.appendChild(card);
        }
    }
}