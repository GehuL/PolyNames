const baseURL = "http://localhost:8080";

export class ApiService
{ 
    static async swapRole()
    {
        const id_partie= JSON.parse(localStorage.getItem("current_player")).idPartie;
        return await fetch(baseURL+"/role/swap/"+id_partie,{method:"post"});;
    }

    /** Commence la partie et change de page
    * @param {*} randomly Indique si il faut démarrer la partie avec les roles aléatoires
     */
    static async startGame(randomly)
    {
        const partieId = JSON.parse(localStorage.getItem("current_player")).idPartie;

        let url = baseURL + "/start/";
       
        if(randomly)
            url += 'random/';

        return await fetch(url+partieId, {"method": "put"})
    }
}